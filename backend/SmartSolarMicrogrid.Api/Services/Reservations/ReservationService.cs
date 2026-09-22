/*
 * File Name: ReservationService.cs
 * Project: Smart Solar Microgrid Trading System
 * Module: SE4040 Enterprise Application Development
 * Author: Isara
 * Description: Service implementation for reservation logic including business rules.
 * Date: 2026-09-14
 */

using SmartSolarMicrogrid.Api.Models.DTOs;
using SmartSolarMicrogrid.Api.Models.Entities;
using SmartSolarMicrogrid.Api.Models.Enums;
using SmartSolarMicrogrid.Api.Repositories;
using SmartSolarMicrogrid.Api.Repositories.Stations;

namespace SmartSolarMicrogrid.Api.Services.Reservations;

public class ReservationService : IReservationService
{
    private readonly IEnergyReservationRepository _reservationRepository;
    private readonly IEnergyBookingSlotRepository _slotRepository;
    private readonly ISolarStationInfoRepository _stationRepository;

    public ReservationService(IEnergyReservationRepository reservationRepository, IEnergyBookingSlotRepository slotRepository, ISolarStationInfoRepository stationRepository)
    {
        _reservationRepository = reservationRepository;
        _slotRepository = slotRepository;
        _stationRepository = stationRepository;
    }

    public async Task<IEnumerable<ReservationDto>> GetReservationsAsync(string? nic, string? stationId, string? status, DateTime? date)
    {
        var all = await _reservationRepository.GetAllAsync();
        
        var filtered = all.AsEnumerable();

        if (!string.IsNullOrEmpty(nic))
            filtered = filtered.Where(r => r.ProsumerNic == nic);
        
        if (!string.IsNullOrEmpty(stationId))
            filtered = filtered.Where(r => r.StationId == stationId);
        
        if (!string.IsNullOrEmpty(status) && Enum.TryParse<ReservationStatus>(status, true, out var parsedStatus))
            filtered = filtered.Where(r => r.Status == parsedStatus);

        if (date.HasValue)
            filtered = filtered.Where(r => r.ScheduledStartDateTime.Date == date.Value.Date);

        var dtos = filtered.Select(MapToDto).ToList();
        
        var allSlots = await _slotRepository.GetAllAsync();
        var allStations = await _stationRepository.GetAllAsync();
        foreach(var dto in dtos)
        {
            var slot = allSlots.FirstOrDefault(s => s.SlotId == dto.SlotId);
            dto.SlotName = slot?.SlotName ?? "Unknown Slot";
            dto.EnergyAmount = slot?.Capacity ?? 0;
            
            var station = allStations.FirstOrDefault(s => s.StationId == dto.StationId);
            dto.StationName = station?.StationName ?? "Unknown Station";
        }
        
        return dtos;
    }

    /// <summary>
    /// Gets the prosumer's current bookings: APPROVED reservations that have not yet started.
    /// </summary>
    public async Task<IEnumerable<ReservationDto>> GetCurrentReservationsByNicAsync(string nic)
    {
        var reservations = await GetReservationsAsync(nic, null, null, null);
        var now = DateTime.UtcNow;
        return reservations
            .Where(r => r.Status == ReservationStatus.APPROVED && r.ScheduledStartDateTime >= now)
            .OrderBy(r => r.ScheduledStartDateTime)
            .ToList();
    }

    /// <summary>
    /// Gets the prosumer's pending bookings awaiting approval.
    /// </summary>
    public async Task<IEnumerable<ReservationDto>> GetPendingReservationsByNicAsync(string nic)
    {
        var reservations = await GetReservationsAsync(nic, null, null, null);
        return reservations
            .Where(r => r.Status == ReservationStatus.PENDING)
            .OrderBy(r => r.ScheduledStartDateTime)
            .ToList();
    }

    /// <summary>
    /// Gets the prosumer's booking history: completed, cancelled, or past reservations.
    /// </summary>
    public async Task<IEnumerable<ReservationDto>> GetHistoryReservationsByNicAsync(string nic)
    {
        var reservations = await GetReservationsAsync(nic, null, null, null);
        var now = DateTime.UtcNow;
        return reservations
            .Where(r => r.Status == ReservationStatus.COMPLETED
                        || r.Status == ReservationStatus.CANCELLED
                        || r.ScheduledStartDateTime < now)
            .OrderByDescending(r => r.ScheduledStartDateTime)
            .ToList();
    }

    public async Task<ReservationDto?> GetReservationByIdAsync(string id)
    {
        var reservation = await _reservationRepository.GetByIdAsync(id);
        if (reservation == null) return null;
        
        var dto = MapToDto(reservation);
        var slot = await _slotRepository.GetByIdAsync(dto.SlotId);
        dto.SlotName = slot?.SlotName ?? "Unknown Slot";
        dto.EnergyAmount = slot?.Capacity ?? 0;
        
        var station = await _stationRepository.GetByIdAsync(dto.StationId);
        dto.StationName = station?.StationName ?? "Unknown Station";
        
        return dto;
    }

    public async Task<(bool Success, string Message, ReservationDto? Reservation)> CreateReservationAsync(string prosumerNic, CreateReservationDto request)
    {
        var slot = await _slotRepository.GetByIdAsync(request.SlotId);
        if (slot == null || slot.StationId != request.StationId)
        {
            return (false, "Invalid slot or station mismatch.", null);
        }

        if (slot.Status != SlotStatus.AVAILABLE)
        {
            return (false, "Selected slot is not available.", null);
        }

        // Rule: Booking must be scheduled within 7 days from today.
        var maxDate = DateTime.UtcNow.Date.AddDays(7);
        if (slot.StartDateTime.Date < DateTime.UtcNow.Date || slot.StartDateTime.Date > maxDate)
        {
            return (false, "Reservation date must be within the next 7 days.", null);
        }

        // Check if the slot is already booked by another active reservation (PENDING or APPROVED)
        var allReservations = await _reservationRepository.GetAllAsync();
        var isSlotBooked = allReservations.Any(r => 
            r.SlotId == request.SlotId && 
            (r.Status == ReservationStatus.PENDING || r.Status == ReservationStatus.APPROVED));
            
        if (isSlotBooked)
        {
            return (false, "This slot is already pending approval or booked by another prosumer.", null);
        }

        // Note: We update the slot status to PENDING so it is no longer available.
        slot.Status = SlotStatus.PENDING;
        await _slotRepository.UpdateAsync(slot.SlotId!, slot);
        var reservationId = MongoDB.Bson.ObjectId.GenerateNewId().ToString();
        var resNumber = "RES-" + new Random().Next(10000, 99999);

        var reservation = new EnergyReservation
        {
            ReservationId = reservationId,
            ReservationNumber = resNumber,
            ProsumerNic = prosumerNic,
            StationId = request.StationId,
            SlotId = request.SlotId,
            ScheduledStartDateTime = slot.StartDateTime,
            ScheduledEndDateTime = slot.EndDateTime,
            Status = ReservationStatus.PENDING,
            QrReference = Guid.NewGuid().ToString("N"), // Generate unique QR ref
            CreatedAt = DateTime.UtcNow,
            UpdatedAt = DateTime.UtcNow,
            Notes = request.Notes
        };

        await _reservationRepository.CreateAsync(reservation);
        
        var dto = MapToDto(reservation);
        dto.SlotName = slot.SlotName;
        dto.EnergyAmount = slot.Capacity;
        
        var station = await _stationRepository.GetByIdAsync(reservation.StationId);
        dto.StationName = station?.StationName ?? "Unknown Station";
        
        return (true, "Reservation created successfully.", dto);
    }

    public async Task<(bool Success, string Message, ReservationDto? Reservation)> UpdateReservationAsync(string id, string prosumerNic, string role, UpdateReservationDto request)
    {
        var reservation = await _reservationRepository.GetByIdAsync(id);
        if (reservation == null)
        {
            return (false, "Reservation not found.", null);
        }
        
        if (role != "GRID_OPERATOR" && role != "BACKOFFICE" && reservation.ProsumerNic != prosumerNic)
        {
            return (false, "Reservation not found or unauthorized.", null);
        }

        // Rule: Cannot update within 12 hours of the reservation start date
        var timeUntilReservation = reservation.ScheduledStartDateTime - DateTime.UtcNow;
        if (timeUntilReservation.TotalHours < 12)
        {
            return (false, "Updates are not allowed within 12 hours of the scheduled reservation date.", null);
        }

        if (reservation.Status != ReservationStatus.PENDING && reservation.Status != ReservationStatus.APPROVED)
        {
            return (false, "Only PENDING or APPROVED reservations can be updated.", null);
        }

        bool slotChanged = reservation.SlotId != request.SlotId;
        bool noteChanged = reservation.Notes != request.Notes;

        if (!slotChanged && !noteChanged)
        {
            return (true, "No changes made.", MapToDto(reservation));
        }

        EnergyBookingSlot? newSlot = null;
        if (slotChanged)
        {
            var oldSlot = await _slotRepository.GetByIdAsync(reservation.SlotId);
            newSlot = await _slotRepository.GetByIdAsync(request.SlotId);

            if (oldSlot == null || newSlot == null)
                return (false, "Invalid slot reference.", null);

            if (newSlot.Status != SlotStatus.AVAILABLE)
            {
                return (false, "New selected slot is not available.", null);
            }
            
            // Check if the new slot is already booked by another active reservation
            var allReservations = await _reservationRepository.GetAllAsync();
            var isSlotBooked = allReservations.Any(r => 
                r.SlotId == request.SlotId && 
                (r.Status == ReservationStatus.PENDING || r.Status == ReservationStatus.APPROVED));
                
            if (isSlotBooked)
            {
                return (false, "The selected slot is already pending approval or booked by another prosumer.", null);
            }
            
            // If the reservation is already approved, update the slot statuses accordingly
            if (reservation.Status == ReservationStatus.APPROVED)
            {
                oldSlot.Status = SlotStatus.AVAILABLE;
                await _slotRepository.UpdateAsync(oldSlot.SlotId!, oldSlot);

                newSlot.Status = SlotStatus.RESERVED;
                await _slotRepository.UpdateAsync(newSlot.SlotId!, newSlot);
            }
            else if (reservation.Status == ReservationStatus.PENDING)
            {
                oldSlot.Status = SlotStatus.AVAILABLE;
                await _slotRepository.UpdateAsync(oldSlot.SlotId!, oldSlot);

                newSlot.Status = SlotStatus.PENDING;
                await _slotRepository.UpdateAsync(newSlot.SlotId!, newSlot);
            }

            reservation.SlotId = request.SlotId;
            reservation.ScheduledStartDateTime = newSlot.StartDateTime;
            reservation.ScheduledEndDateTime = newSlot.EndDateTime;
        }

        reservation.Notes = request.Notes;
        reservation.UpdatedAt = DateTime.UtcNow;
        
        await _reservationRepository.UpdateAsync(id, reservation);
        
        var dto = MapToDto(reservation);
        if (newSlot != null)
        {
            dto.SlotName = newSlot.SlotName;
            dto.EnergyAmount = newSlot.Capacity;
        }
        else 
        {
            var currentSlot = await _slotRepository.GetByIdAsync(reservation.SlotId);
            dto.SlotName = currentSlot?.SlotName ?? "Unknown Slot";
            dto.EnergyAmount = currentSlot?.Capacity ?? 0;
        }
        
        var station = await _stationRepository.GetByIdAsync(reservation.StationId);
        dto.StationName = station?.StationName ?? "Unknown Station";

        return (true, "Reservation updated successfully.", dto);
    }

    public async Task<(bool Success, string Message)> CancelReservationAsync(string id, string prosumerNic, string role)
    {
        var reservation = await _reservationRepository.GetByIdAsync(id);
        if (reservation == null)
        {
            return (false, "Reservation not found or unauthorized.");
        }

        if (role != "GRID_OPERATOR" && role != "BACKOFFICE" && reservation.ProsumerNic != prosumerNic)
        {
            return (false, "Reservation not found or unauthorized.");
        }

        // Rule: Cannot cancel within 12 hours of the reservation start date
        var timeUntilReservation = reservation.ScheduledStartDateTime - DateTime.UtcNow;
        if (timeUntilReservation.TotalHours < 12)
        {
            return (false, "Cancellations are not allowed within 12 hours of the scheduled reservation date.");
        }

        if (reservation.Status != ReservationStatus.PENDING && reservation.Status != ReservationStatus.APPROVED)
        {
            return (false, "Only PENDING or APPROVED reservations can be cancelled.");
        }

        // Return capacity to slot by marking as AVAILABLE
        var slot = await _slotRepository.GetByIdAsync(reservation.SlotId);
        if (slot != null)
        {
            slot.Status = SlotStatus.AVAILABLE;
            await _slotRepository.UpdateAsync(slot.SlotId!, slot);
        }

        reservation.Status = ReservationStatus.CANCELLED;
        reservation.UpdatedAt = DateTime.UtcNow;
        await _reservationRepository.UpdateAsync(id, reservation);

        return (true, "Reservation cancelled successfully.");
    }

    public async Task<(bool Success, string Message, ReservationDto? Reservation)> UpdateReservationStatusAsync(string id, ReservationStatus newStatus)
    {
        if (newStatus == ReservationStatus.COMPLETED || newStatus == ReservationStatus.CANCELLED)
        {
            return (false, "Status must be updated via the dedicated cancellation endpoint or QR verification flow.", null);
        }

        var reservation = await _reservationRepository.GetByIdAsync(id);
        if (reservation == null)
        {
            return (false, "Reservation not found.", null);
        }

        reservation.Status = newStatus;
        reservation.UpdatedAt = DateTime.UtcNow;

        if (newStatus == ReservationStatus.COMPLETED)
        {
            reservation.CompletedAt = DateTime.UtcNow;
            
            // Release the slot back to AVAILABLE
            var slotToRelease = await _slotRepository.GetByIdAsync(reservation.SlotId);
            if (slotToRelease != null && (slotToRelease.Status == SlotStatus.RESERVED || slotToRelease.Status == SlotStatus.PENDING))
            {
                slotToRelease.Status = SlotStatus.AVAILABLE;
                await _slotRepository.UpdateAsync(slotToRelease.SlotId!, slotToRelease);
            }
        }
        else if (newStatus == ReservationStatus.APPROVED)
        {
            // Reserve the slot physically now that it's approved
            var slotToReserve = await _slotRepository.GetByIdAsync(reservation.SlotId);
            if (slotToReserve != null && (slotToReserve.Status == SlotStatus.AVAILABLE || slotToReserve.Status == SlotStatus.PENDING))
            {
                slotToReserve.Status = SlotStatus.RESERVED;
                await _slotRepository.UpdateAsync(slotToReserve.SlotId!, slotToReserve);
            }
        }

        await _reservationRepository.UpdateAsync(id, reservation);
        
        var dto = MapToDto(reservation);
        var slot = await _slotRepository.GetByIdAsync(reservation.SlotId);
        dto.SlotName = slot?.SlotName ?? "Unknown Slot";
        dto.EnergyAmount = slot?.Capacity ?? 0;
        
        var station = await _stationRepository.GetByIdAsync(reservation.StationId);
        dto.StationName = station?.StationName ?? "Unknown Station";

        return (true, "Reservation status updated successfully.", dto);
    }

    private static ReservationDto MapToDto(EnergyReservation reservation)
    {
        return new ReservationDto
        {
            ReservationId = reservation.ReservationId!,
            ReservationNumber = reservation.ReservationNumber,
            ProsumerNic = reservation.ProsumerNic,
            StationId = reservation.StationId,
            SlotId = reservation.SlotId,
            ScheduledStartDateTime = reservation.ScheduledStartDateTime,
            ScheduledEndDateTime = reservation.ScheduledEndDateTime,
            Status = reservation.Status,
            QrReference = reservation.QrReference,
            CreatedAt = reservation.CreatedAt,
            UpdatedAt = reservation.UpdatedAt,
            CompletedAt = reservation.CompletedAt,
            Notes = reservation.Notes
        };
    }
}
