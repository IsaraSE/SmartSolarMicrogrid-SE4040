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

    /// <summary>
    /// Retrieves all solar stations in the system.
    /// </summary>
    /// <returns>A collection of all stations mapped to DTOs.</returns>
    public async Task<IEnumerable<StationDto>> GetAllStationsAsync()
    {
        var stations = await _stationRepository.GetAllAsync();
        return stations.Select(MapToDto);
    }

    /// <summary>
    /// Retrieves a single solar station by its identifier.
    /// </summary>
    /// <param name="id">The station identifier.</param>
    /// <returns>The matching station DTO, or null if no station exists with the given id.</returns>
    public async Task<StationDto?> GetStationByIdAsync(string id)
    {
        var station = await _stationRepository.GetByIdAsync(id);
        return station != null ? MapToDto(station) : null;
    }

    /// <summary>
    /// Creates a new solar station with an active status.
    /// </summary>
    /// <param name="request">The station details to create.</param>
    /// <returns>The newly created station as a DTO.</returns>
    public async Task<StationDto> CreateStationAsync(CreateStationDto request)
    {
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

    /// <summary>
    /// Updates an existing solar station's details. Deactivation is blocked while
    /// the station has pending or approved reservations, to avoid stranding prosumers.
    /// </summary>
    /// <param name="id">The identifier of the station to update.</param>
    /// <param name="request">The updated station details.</param>
    /// <returns>A tuple indicating success, a status message, and the updated station DTO (null on failure).</returns>
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

    /// <summary>
    /// Safely deactivates a solar station, refusing the operation if the station
    /// still has active (pending or approved) reservations.
    /// </summary>
    /// <param name="id">The identifier of the station to deactivate.</param>
    /// <returns>A tuple indicating success, a status message, and the resulting station DTO (null on failure).</returns>
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

    /// <summary>
    /// Permanently removes a solar station, refusing the operation if it still
    /// has active reservations associated with it.
    /// </summary>
    /// <param name="id">The identifier of the station to delete.</param>
    /// <returns>True if the station was deleted; false if no station was found with the given id.</returns>
    /// <exception cref="InvalidOperationException">Thrown when the station has active reservations.</exception>
    public async Task<bool> DeleteStationAsync(string id)
    {
        var station = await _stationRepository.GetByIdAsync(id);
        if (station == null) return false;

        var activeReservations = await _reservationRepository.GetActiveReservationsByStationIdAsync(id);
        if (activeReservations.Any())
        {
            throw new InvalidOperationException("Cannot delete a station with active reservations.");
        }

        await _stationRepository.DeleteAsync(id);
        return true;
    }

    /// <summary>
    /// Maps a station entity to its corresponding DTO representation.
    /// </summary>
    private static StationDto MapToDto(SolarStationInfo station)
    {
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
