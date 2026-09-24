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

    public async Task<IEnumerable<SlotDto>> GetAllSlotsAsync()
    {
        // IBaseRepository provides GetAllAsync
        var slots = await _slotRepository.GetAllAsync();
        return slots.Select(MapToDto);
    }

    public async Task<IEnumerable<SlotDto>> GetSlotsByStationIdAsync(string stationId)
    {
        // Retrieves slots by station id data from the system.
        var slots = await _slotRepository.GetByStationIdAsync(stationId);
        return slots.Select(MapToDto);
    }

    public async Task<IEnumerable<SlotDto>> GetAvailableSlotsByStationIdAsync(string stationId)
    {
        // Retrieves available slots by station id data from the system.
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

    public async Task<SlotDto> CreateSlotAsync(CreateSlotDto request)
    {
        // Handles the creation of slot.
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

    public async Task<SlotDto?> UpdateSlotAsync(string id, UpdateSlotDto request)
    {
        // Updates existing slot records.
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

    public async Task<bool> DeleteSlotAsync(string id)
    {
        // Safely removes slot from the database.
        var slot = await _slotRepository.GetByIdAsync(id);
        if (slot == null) return false;

        if (slot.Status == SlotStatus.RESERVED)
        {
            throw new InvalidOperationException("Cannot delete a booked slot.");
        }

        await _slotRepository.DeleteAsync(id);
        return true;
    }

    private static SlotDto MapToDto(EnergyBookingSlot slot)
    {
        // Maps to dto to the corresponding DTO.
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
