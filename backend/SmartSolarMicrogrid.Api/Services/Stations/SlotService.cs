/*
 * File Name: SlotService.cs
 * Project: Smart Solar Microgrid Trading System
 * Module: SE4040 Enterprise Application Development
 * Author: IT22154880
 * Description: Implementation of SlotService.cs
 * Date: 2026-09-19
 */

using SmartSolarMicrogrid.Api.Models.DTOs;
using SmartSolarMicrogrid.Api.Models.Entities;
using SmartSolarMicrogrid.Api.Models.Enums;
using SmartSolarMicrogrid.Api.Repositories;

namespace SmartSolarMicrogrid.Api.Services.Stations;

public class SlotService : ISlotService
{
    private readonly IEnergyBookingSlotRepository _slotRepository;
    private readonly ISolarStationInfoRepository _stationRepository;
    private readonly SmartSolarMicrogrid.Api.Repositories.Reservations.IEnergyReservationRepository _reservationRepository;

    public SlotService(
        IEnergyBookingSlotRepository slotRepository, 
        ISolarStationInfoRepository stationRepository,
        SmartSolarMicrogrid.Api.Repositories.Reservations.IEnergyReservationRepository reservationRepository)
    {
        _slotRepository = slotRepository;
        _stationRepository = stationRepository;
        _reservationRepository = reservationRepository;
    }

    /// <summary>
    /// Retrieves every booking slot across all stations.
    /// </summary>
    /// <returns>A collection of all slots mapped to DTOs.</returns>
    public async Task<IEnumerable<SlotDto>> GetAllSlotsAsync()
    {
        var slots = await _slotRepository.GetAllAsync();
        return slots.Select(MapToDto);
    }

    /// <summary>
    /// Retrieves every booking slot belonging to a specific station, regardless of availability.
    /// </summary>
    /// <param name="stationId">The station identifier.</param>
    /// <returns>A collection of the station's slots mapped to DTOs.</returns>
    public async Task<IEnumerable<SlotDto>> GetSlotsByStationIdAsync(string stationId)
    {
        var slots = await _slotRepository.GetByStationIdAsync(stationId);
        return slots.Select(MapToDto);
    }

    /// <summary>
    /// Retrieves the slots for a station that are currently free to book: their status
    /// is AVAILABLE and they are not tied to a pending or approved reservation.
    /// </summary>
    /// <param name="stationId">The station identifier.</param>
    /// <returns>A collection of bookable slots mapped to DTOs.</returns>
    public async Task<IEnumerable<SlotDto>> GetAvailableSlotsByStationIdAsync(string stationId)
    {
        var slots = await _slotRepository.GetByStationIdAsync(stationId);
        
        // Fetch all active reservations for this station
        var activeReservations = await _reservationRepository.GetActiveReservationsByStationIdAsync(stationId);
        var bookedSlotIds = activeReservations
            .Where(r => r.Status == ReservationStatus.PENDING || r.Status == ReservationStatus.APPROVED)
            .Select(r => r.SlotId)
            .ToHashSet();

        // A slot is available if its status is AVAILABLE and it is not already booked
        var availableSlots = slots.Where(s => s.Status == SlotStatus.AVAILABLE && !bookedSlotIds.Contains(s.SlotId));

        return availableSlots.Select(MapToDto);
    }

    /// <summary>
    /// Creates a new booking slot for a station, enforcing a unique slot name per
    /// station and the station's overall battery slot capacity.
    /// </summary>
    /// <param name="request">The slot details to create.</param>
    /// <returns>The newly created slot as a DTO.</returns>
    /// <exception cref="InvalidOperationException">
    /// Thrown when the station does not exist, the slot name is already taken for
    /// the station, or the station's slot capacity has been reached.
    /// </exception>
    public async Task<SlotDto> CreateSlotAsync(CreateSlotDto request)
    {
        var station = await _stationRepository.GetByIdAsync(request.StationId);
        if (station == null)
            throw new InvalidOperationException("Station not found.");

        var existingSlots = await _slotRepository.GetByStationIdAsync(request.StationId);
        
        // Prevent duplicate SlotName for the same Station
        var duplicateName = existingSlots.Any(s => s.SlotName == request.SlotName);
        if (duplicateName)
        {
            throw new InvalidOperationException($"Slot with name {request.SlotName} already exists for this station.");
        }

        // Prevent exceeding station capacity
        if (existingSlots.Count() >= station.BatterySlotCount)
        {
            throw new InvalidOperationException($"Cannot create more slots. Station capacity ({station.BatterySlotCount}) reached.");
        }

        var slot = new EnergyBookingSlot
        {
            SlotId = MongoDB.Bson.ObjectId.GenerateNewId().ToString(),
            SlotName = request.SlotName,
            StationId = request.StationId,
            StartDateTime = request.StartDateTime,
            EndDateTime = request.EndDateTime,
            Capacity = request.Capacity,
            Notes = request.Notes,
            Status = SlotStatus.AVAILABLE
        };

        await _slotRepository.CreateAsync(slot);
        return MapToDto(slot);
    }

    /// <summary>
    /// Updates the schedule, capacity, status and notes of an existing slot.
    /// </summary>
    /// <param name="id">The identifier of the slot to update.</param>
    /// <param name="request">The updated slot details.</param>
    /// <returns>The updated slot DTO, or null if no slot exists with the given id.</returns>
    public async Task<SlotDto?> UpdateSlotAsync(string id, UpdateSlotDto request)
    {
        var slot = await _slotRepository.GetByIdAsync(id);
        if (slot == null) return null;

        slot.StartDateTime = request.StartDateTime;
        slot.EndDateTime = request.EndDateTime;
        slot.Capacity = request.Capacity;
        slot.Status = request.Status;
        slot.Notes = request.Notes;

        await _slotRepository.UpdateAsync(id, slot);
        return MapToDto(slot);
    }

    /// <summary>
    /// Permanently removes a booking slot, refusing the operation if the slot is
    /// currently reserved.
    /// </summary>
    /// <param name="id">The identifier of the slot to delete.</param>
    /// <returns>True if the slot was deleted; false if no slot was found with the given id.</returns>
    /// <exception cref="InvalidOperationException">Thrown when the slot is currently reserved.</exception>
    public async Task<bool> DeleteSlotAsync(string id)
    {
        var slot = await _slotRepository.GetByIdAsync(id);
        if (slot == null) return false;

        if (slot.Status == SlotStatus.RESERVED)
        {
            throw new InvalidOperationException("Cannot delete a booked slot.");
        }

        await _slotRepository.DeleteAsync(id);
        return true;
    }

    /// <summary>
    /// Maps a slot entity to its corresponding DTO representation.
    /// </summary>
    private static SlotDto MapToDto(EnergyBookingSlot slot)
    {
        return new SlotDto
        {
            SlotId = slot.SlotId!,
            SlotName = slot.SlotName,
            StationId = slot.StationId,
            StartDateTime = slot.StartDateTime,
            EndDateTime = slot.EndDateTime,
            Capacity = slot.Capacity,
            Status = slot.Status,
            ReservedBy = slot.ReservedBy,
            Notes = slot.Notes
        };
    }
}
