/*
 * File Name: ISlotService.cs
 * Project: Smart Solar Microgrid Trading System
 * Module: SE4040 Enterprise Application Development
 * Author: IT22154880
 * Description: Implementation of ISlotService.cs
 * Date: 2026-09-15
 */

using SmartSolarMicrogrid.Api.Models.DTOs;

namespace SmartSolarMicrogrid.Api.Services.Stations;

public interface ISlotService
{
    Task<IEnumerable<SlotDto>> GetAllSlotsAsync();
    Task<IEnumerable<SlotDto>> GetSlotsByStationIdAsync(string stationId);
    Task<IEnumerable<SlotDto>> GetAvailableSlotsByStationIdAsync(string stationId);
    Task<SlotDto> CreateSlotAsync(CreateSlotDto request);
    Task<SlotDto?> UpdateSlotAsync(string id, UpdateSlotDto request);
    Task<bool> DeleteSlotAsync(string id);
}
