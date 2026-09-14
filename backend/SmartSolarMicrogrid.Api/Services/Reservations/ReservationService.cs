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

namespace SmartSolarMicrogrid.Api.Services.Reservations;

public class ReservationService : IReservationService
{
    private readonly IEnergyReservationRepository _reservationRepository;
    private readonly IEnergyBookingSlotRepository _slotRepository;

    public ReservationService(IEnergyReservationRepository reservationRepository, IEnergyBookingSlotRepository slotRepository)
    {
        _reservationRepository = reservationRepository;
        _slotRepository = slotRepository;
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

        return filtered.Select(MapToDto);
    }

    public async Task<ReservationDto?> GetReservationByIdAsync(string id)
    {
        var reservation = await _reservationRepository.GetByIdAsync(id);
        return reservation != null ? MapToDto(reservation) : null;
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

        // Update slot status
        slot.Status = SlotStatus.RESERVED;
        await _slotRepository.UpdateAsync(slot.SlotId!, slot);

        var reservation = new EnergyReservation
        {
            ReservationId = MongoDB.Bson.ObjectId.GenerateNewId().ToString(),
            ProsumerNic = prosumerNic,
            StationId = request.StationId,
            SlotId = request.SlotId,
            ScheduledStartDateTime = slot.StartDateTime,
            ScheduledEndDateTime = slot.EndDateTime,
            Status = ReservationStatus.PENDING,
            QrReference = Guid.NewGuid().ToString("N"), // Generate unique QR ref
            CreatedAt = DateTime.UtcNow,
            UpdatedAt = DateTime.UtcNow
        };

        await _reservationRepository.CreateAsync(reservation);
        return (true, "Reservation created successfully.", MapToDto(reservation));
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

        if (reservation.SlotId == request.SlotId)
        {
            return (true, "No changes made.", MapToDto(reservation));
        }

        var oldSlot = await _slotRepository.GetByIdAsync(reservation.SlotId);
        var newSlot = await _slotRepository.GetByIdAsync(request.SlotId);

        if (oldSlot == null || newSlot == null)
            return (false, "Invalid slot reference.", null);

        if (newSlot.Status != SlotStatus.AVAILABLE)
        {
            return (false, "New selected slot is not available.", null);
        }
        
        // Revert old slot
        oldSlot.Status = SlotStatus.AVAILABLE;
        await _slotRepository.UpdateAsync(oldSlot.SlotId!, oldSlot);

        // Update new slot
        newSlot.Status = SlotStatus.RESERVED;
        await _slotRepository.UpdateAsync(newSlot.SlotId!, newSlot);

        reservation.SlotId = request.SlotId;
        reservation.ScheduledStartDateTime = newSlot.StartDateTime;
        reservation.ScheduledEndDateTime = newSlot.EndDateTime;
        reservation.UpdatedAt = DateTime.UtcNow;
        
        await _reservationRepository.UpdateAsync(id, reservation);

        return (true, "Reservation updated successfully.", MapToDto(reservation));
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
        }

        await _reservationRepository.UpdateAsync(id, reservation);

        return (true, "Reservation status updated successfully.", MapToDto(reservation));
    }

    private static ReservationDto MapToDto(EnergyReservation reservation)
    {
        return new ReservationDto
        {
            ReservationId = reservation.ReservationId!,
            ProsumerNic = reservation.ProsumerNic,
            StationId = reservation.StationId,
            SlotId = reservation.SlotId,
            ScheduledStartDateTime = reservation.ScheduledStartDateTime,
            ScheduledEndDateTime = reservation.ScheduledEndDateTime,
            Status = reservation.Status,
            QrReference = reservation.QrReference,
            CreatedAt = reservation.CreatedAt,
            UpdatedAt = reservation.UpdatedAt,
            CompletedAt = reservation.CompletedAt
        };
    }
}
