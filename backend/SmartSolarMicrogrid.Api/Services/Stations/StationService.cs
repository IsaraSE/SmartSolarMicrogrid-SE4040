/*
 * File Name: StationService.cs
 * Project: Smart Solar Microgrid Trading System
 * Module: SE4040 Enterprise Application Development
 * Author: IT22154880
 * Description: Implementation of StationService.cs
 * Date: 2026-09-23
 */

using SmartSolarMicrogrid.Api.Models.DTOs;
using SmartSolarMicrogrid.Api.Models.Entities;
using SmartSolarMicrogrid.Api.Models.Enums;
using SmartSolarMicrogrid.Api.Repositories;

namespace SmartSolarMicrogrid.Api.Services.Stations;

public class StationService : IStationService
{
    private readonly ISolarStationInfoRepository _stationRepository;
    private readonly IEnergyReservationRepository _reservationRepository;

    public StationService(ISolarStationInfoRepository stationRepository, IEnergyReservationRepository reservationRepository)
    {
        _stationRepository = stationRepository;
        _reservationRepository = reservationRepository;
    }

    public async Task<IEnumerable<StationDto>> GetAllStationsAsync()
    {
        // Retrieves all stations data from the system.
        var stations = await _stationRepository.GetAllAsync();
        return stations.Select(MapToDto);
    }

    public async Task<StationDto?> GetStationByIdAsync(string id)
    {
        // Retrieves station by id data from the system.
        var station = await _stationRepository.GetByIdAsync(id);
        return station != null ? MapToDto(station) : null;
    }

    public async Task<StationDto> CreateStationAsync(CreateStationDto request)
    {
        // Handles the creation of station.
        var station = new SolarStationInfo
        {
            StationId = MongoDB.Bson.ObjectId.GenerateNewId().ToString(),
            StationName = request.StationName,
            Address = request.Address,
            Latitude = request.Latitude,
            Longitude = request.Longitude,
            Capacity = request.Capacity,
            BatterySlotCount = request.BatterySlotCount,
            OperatingStartTime = request.OperatingStartTime,
            OperatingEndTime = request.OperatingEndTime,
            Description = request.Description,
            Status = StationStatus.ACTIVE
        };

        await _stationRepository.CreateAsync(station);
        return MapToDto(station);
    }

    public async Task<(bool Success, string Message, StationDto? Station)> UpdateStationAsync(string id, UpdateStationDto request)
    {
        var station = await _stationRepository.GetByIdAsync(id);
        if (station == null)
            return (false, "Station not found.", null);

        // Deactivation Rule: Cannot deactivate if there are pending/approved reservations
        if (request.Status == StationStatus.DEACTIVATED && station.Status == StationStatus.ACTIVE)
        {
            var activeReservations = await _reservationRepository.GetActiveReservationsByStationIdAsync(id);
            if (activeReservations.Any())
            {
                return (false, "Cannot deactivate station. There are active reservations associated with it.", null);
            }
        }

        station.StationName = request.StationName;
        station.Address = request.Address;
        station.Latitude = request.Latitude;
        station.Longitude = request.Longitude;
        station.Capacity = request.Capacity;
        station.BatterySlotCount = request.BatterySlotCount;
        station.OperatingStartTime = request.OperatingStartTime;
        station.OperatingEndTime = request.OperatingEndTime;
        station.Description = request.Description;
        station.Status = request.Status;

        await _stationRepository.UpdateAsync(id, station);
        return (true, "Station updated successfully.", MapToDto(station));
    }

    public async Task<(bool Success, string Message, StationDto? Station)> DeactivateStationAsync(string id)
    {
        var station = await _stationRepository.GetByIdAsync(id);
        if (station == null)
            return (false, "Station not found.", null);

        if (station.Status == StationStatus.DEACTIVATED)
            return (true, "Station is already deactivated.", MapToDto(station));

        var activeReservations = await _reservationRepository.GetActiveReservationsByStationIdAsync(id);
        if (activeReservations.Any())
        {
            return (false, "Cannot deactivate station. There are active reservations associated with it.", null);
        }

        station.Status = StationStatus.DEACTIVATED;
        await _stationRepository.UpdateAsync(id, station);
        
        return (true, "Station deactivated successfully.", MapToDto(station));
    }

    public async Task<bool> DeleteStationAsync(string id)
    {
        // Safely removes station from the database.
        var station = await _stationRepository.GetByIdAsync(id);
        if (station == null) return false;

        // Optionally enforce deletion logic based on reservations
        var activeReservations = await _reservationRepository.GetActiveReservationsByStationIdAsync(id);
        if (activeReservations.Any())
        {
            throw new InvalidOperationException("Cannot delete a station with active reservations.");
        }

        await _stationRepository.DeleteAsync(id);
        return true;
    }

    private static StationDto MapToDto(SolarStationInfo station)
    {
        // Maps to dto to the corresponding DTO.
        return new StationDto
        {
            StationId = station.StationId!,
            StationName = station.StationName,
            Address = station.Address,
            Latitude = station.Latitude,
            Longitude = station.Longitude,
            Capacity = station.Capacity,
            BatterySlotCount = station.BatterySlotCount,
            OperatingStartTime = station.OperatingStartTime,
            OperatingEndTime = station.OperatingEndTime,
            Description = station.Description,
            Status = station.Status
        };
    }
}
