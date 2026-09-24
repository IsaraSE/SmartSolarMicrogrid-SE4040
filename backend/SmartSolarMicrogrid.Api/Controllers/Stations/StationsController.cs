/*
 * File Name: StationsController.cs
 * Project: Smart Solar Microgrid Trading System
 * Module: SE4040 Enterprise Application Development
 * Author: IT22154880
 * Description: Implementation of StationsController.cs
 * Date: 2026-09-21
 */

using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using SmartSolarMicrogrid.Api.Models.DTOs;
using SmartSolarMicrogrid.Api.Services;

namespace SmartSolarMicrogrid.Api.Controllers.Stations;

[ApiController]
[Route("api/[controller]")]
[Authorize(Roles = "BACKOFFICE,GRID_OPERATOR,PROSUMER")]
public class StationsController : ControllerBase
{
    private readonly IStationService _stationService;
    private readonly ISlotService _slotService;

    public StationsController(IStationService stationService, ISlotService slotService)
    {
        _stationService = stationService;
        _slotService = slotService;
    }

    /// <summary>
    /// Gets all solar stations.
    /// </summary>
    [HttpGet]
    [AllowAnonymous]
    public async Task<IActionResult> GetAllStations()
    {
        // Retrieves all stations data from the system.
        var stations = await _stationService.GetAllStationsAsync();
        return Ok(ApiResponse<IEnumerable<StationDto>>.SuccessResponse("Stations retrieved successfully.", stations));
    }

    /// <summary>
    /// Gets a specific solar station.
    /// </summary>
    [HttpGet("{id}")]
    public async Task<IActionResult> GetStationById(string id)
    {
        // Retrieves station by id data from the system.
        var station = await _stationService.GetStationByIdAsync(id);
        if (station == null)
        {
            return NotFound(ApiResponse<object>.ErrorResponse("Station not found."));
        }
        return Ok(ApiResponse<StationDto>.SuccessResponse("Station retrieved successfully.", station));
    }

    /// <summary>
    /// Gets available slots for a station (used by the mobile app when creating a booking).
    /// </summary>
    [HttpGet("{id}/available-slots")]
    [AllowAnonymous]
    public async Task<IActionResult> GetAvailableSlots(string id)
    {
        // Retrieves available slots data from the system.
        var slots = await _slotService.GetAvailableSlotsByStationIdAsync(id);
        return Ok(ApiResponse<IEnumerable<SlotDto>>.SuccessResponse("Available slots retrieved successfully.", slots));
    }

    /// <summary>
    /// Creates a new solar station.
    /// </summary>
    [HttpPost]
    [Authorize(Roles = "BACKOFFICE")]
    public async Task<IActionResult> CreateStation([FromBody] CreateStationDto request)
    {
        // Handles the creation of station.
        if (!ModelState.IsValid)
        {
            return BadRequest(ApiResponse<object>.ErrorResponse("Invalid request data."));
        }

        var station = await _stationService.CreateStationAsync(request);
        return CreatedAtAction(nameof(GetStationById), new { id = station.StationId }, ApiResponse<StationDto>.SuccessResponse("Station created successfully.", station));
    }

    /// <summary>
    /// Updates an existing solar station.
    /// </summary>
    [HttpPut("{id}")]
    [Authorize(Roles = "BACKOFFICE")]
    public async Task<IActionResult> UpdateStation(string id, [FromBody] UpdateStationDto request)
    {
        // Updates existing station records.
        if (!ModelState.IsValid)
        {
            return BadRequest(ApiResponse<object>.ErrorResponse("Invalid request data."));
        }

        var (success, message, station) = await _stationService.UpdateStationAsync(id, request);
        if (!success)
        {
            return BadRequest(ApiResponse<object>.ErrorResponse(message));
        }
        return Ok(ApiResponse<StationDto>.SuccessResponse(message, station));
    }

    /// <summary>
    /// Deactivates a solar station safely.
    /// </summary>
    [HttpPut("{id}/deactivate")]
    [Authorize(Roles = "BACKOFFICE")]
    public async Task<IActionResult> DeactivateStation(string id)
    {
        // Deactivates the specified station.
        var (success, message, station) = await _stationService.DeactivateStationAsync(id);
        if (!success)
        {
            return BadRequest(ApiResponse<object>.ErrorResponse(message));
        }

        return Ok(ApiResponse<StationDto>.SuccessResponse(message, station));
    }
}
