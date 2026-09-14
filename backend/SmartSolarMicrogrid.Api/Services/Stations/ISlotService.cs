/*
 * File Name: ISlotService.cs
 * Project: Smart Solar Microgrid Trading System
 * Module: SE4040 Enterprise Application Development
 * Author: Isara
 * Description: Service interface for slot management logic.
 * Date: 2026-09-14
 */

using SmartSolarMicrogrid.Api.Models.DTOs;

namespace SmartSolarMicrogrid.Api.Services.Stations;

public interface ISlotService
{
    Task<IEnumerable<SlotDto>> GetSlotsByStationIdAsync(string stationId);
    Task<IEnumerable<SlotDto>> GetAvailableSlotsByStationIdAsync(string stationId);
    Task<SlotDto> CreateSlotAsync(CreateSlotDto request);
    Task<SlotDto?> UpdateSlotAsync(string id, UpdateSlotDto request);
    Task<bool> DeleteSlotAsync(string id);
}
