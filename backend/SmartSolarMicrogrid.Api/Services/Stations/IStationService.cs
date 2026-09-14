/*
 * File Name: IStationService.cs
 * Project: Smart Solar Microgrid Trading System
 * Module: SE4040 Enterprise Application Development
 * Author: Isara
 * Description: Service interface for solar station logic.
 * Date: 2026-09-14
 */

using SmartSolarMicrogrid.Api.Models.DTOs;

namespace SmartSolarMicrogrid.Api.Services.Stations;

public interface IStationService
{
    Task<IEnumerable<StationDto>> GetAllStationsAsync();
    Task<StationDto?> GetStationByIdAsync(string id);
    Task<StationDto> CreateStationAsync(CreateStationDto request);
    Task<(bool Success, string Message, StationDto? Station)> UpdateStationAsync(string id, UpdateStationDto request);
    Task<(bool Success, string Message, StationDto? Station)> DeactivateStationAsync(string id);
    Task<bool> DeleteStationAsync(string id);
}
