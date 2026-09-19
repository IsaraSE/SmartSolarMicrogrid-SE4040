/*
 * File Name: SlotService.cs
 * Project: Smart Solar Microgrid Trading System
 * Module: SE4040 Enterprise Application Development
 * Author: Isara
 * Description: Service implementation for slot management logic.
 * Date: 2026-09-14
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

    public SlotService(IEnergyBookingSlotRepository slotRepository, ISolarStationInfoRepository stationRepository)
    {
        _slotRepository = slotRepository;
        _stationRepository = stationRepository;
    }

    public async Task<IEnumerable<SlotDto>> GetSlotsByStationIdAsync(string stationId)
    {
        var slots = await _slotRepository.GetByStationIdAsync(stationId);
        return slots.Select(MapToDto);
    }

    public async Task<IEnumerable<SlotDto>> GetAvailableSlotsByStationIdAsync(string stationId)
    {
        var slots = await _slotRepository.GetByStationIdAsync(stationId);
        var availableSlots = slots.Where(s => s.Status == SlotStatus.AVAILABLE);
        return availableSlots.Select(MapToDto);
    }

    public async Task<SlotDto> CreateSlotAsync(CreateSlotDto request)
    {
        // Prevent duplicate time overlap for the same SlotName at the same Station
        var existingSlots = await _slotRepository.GetByStationIdAsync(request.StationId);
        var overlap = existingSlots.Any(s => 
            s.SlotName == request.SlotName &&
            (request.StartDateTime < s.EndDateTime && request.EndDateTime > s.StartDateTime));
        
        if (overlap)
        {
            throw new InvalidOperationException($"Time overlap: Slot {request.SlotName} is already scheduled during this time.");
        }

        var slot = new EnergyBookingSlot
        {
            SlotId = MongoDB.Bson.ObjectId.GenerateNewId().ToString(),
            SlotName = request.SlotName,
            StationId = request.StationId,
            StartDateTime = request.StartDateTime,
            EndDateTime = request.EndDateTime,
            Notes = request.Notes,
            Status = SlotStatus.AVAILABLE
        };

        await _slotRepository.CreateAsync(slot);
        return MapToDto(slot);
    }

    public async Task<SlotDto?> UpdateSlotAsync(string id, UpdateSlotDto request)
    {
        var slot = await _slotRepository.GetByIdAsync(id);
        if (slot == null) return null;

        // Prevent duplicate time overlap for the same SlotName at the same Station on update
        var existingSlots = await _slotRepository.GetByStationIdAsync(slot.StationId);
        var overlap = existingSlots.Any(s => 
            s.SlotId != slot.SlotId && 
            s.SlotName == slot.SlotName &&
            (request.StartDateTime < s.EndDateTime && request.EndDateTime > s.StartDateTime));
            
        if (overlap)
        {
            throw new InvalidOperationException($"Time overlap: Slot {slot.SlotName} is already scheduled during this time.");
        }

        slot.StartDateTime = request.StartDateTime;
        slot.EndDateTime = request.EndDateTime;
        slot.Status = request.Status;
        slot.Notes = request.Notes;

        await _slotRepository.UpdateAsync(id, slot);
        return MapToDto(slot);
    }

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

    private static SlotDto MapToDto(EnergyBookingSlot slot)
    {
        return new SlotDto
        {
            SlotId = slot.SlotId!,
            SlotName = slot.SlotName,
            StationId = slot.StationId,
            StartDateTime = slot.StartDateTime,
            EndDateTime = slot.EndDateTime,
            Status = slot.Status,
            ReservedBy = slot.ReservedBy,
            Notes = slot.Notes
        };
    }
}
