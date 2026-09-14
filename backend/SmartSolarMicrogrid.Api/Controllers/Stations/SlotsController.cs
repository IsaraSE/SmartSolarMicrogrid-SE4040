/*
 * File Name: SlotsController.cs
 * Project: Smart Solar Microgrid Trading System
 * Module: SE4040 Enterprise Application Development
 * Author: Isara
 * Description: Controller for solar station slots operations.
 * Date: 2026-09-14
 */

using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using SmartSolarMicrogrid.Api.Models.DTOs;
using SmartSolarMicrogrid.Api.Services;

namespace SmartSolarMicrogrid.Api.Controllers.Stations;

[ApiController]
[Route("api/[controller]")]
[Authorize]
public class SlotsController : ControllerBase
{
    private readonly ISlotService _slotService;

    public SlotsController(ISlotService slotService)
    {
        _slotService = slotService;
    }

    /// <summary>
    /// Gets all slots for a station.
    /// </summary>
    [HttpGet("station/{stationId}")]
    [Authorize(Roles = "BACKOFFICE,GRID_OPERATOR")]
    public async Task<IActionResult> GetSlotsByStationId(string stationId)
    {
        var slots = await _slotService.GetSlotsByStationIdAsync(stationId);
        return Ok(ApiResponse<IEnumerable<SlotDto>>.SuccessResponse("Slots retrieved successfully.", slots));
    }

    /// <summary>
    /// Gets available slots for a station.
    /// </summary>
    [HttpGet("station/{stationId}/available")]
    [Authorize(Roles = "BACKOFFICE,GRID_OPERATOR,PROSUMER")]
    public async Task<IActionResult> GetAvailableSlotsByStationId(string stationId)
    {
        var slots = await _slotService.GetAvailableSlotsByStationIdAsync(stationId);
        return Ok(ApiResponse<IEnumerable<SlotDto>>.SuccessResponse("Available slots retrieved successfully.", slots));
    }

    /// <summary>
    /// Creates a new slot (Operator/Backoffice).
    /// </summary>
    [HttpPost]
    [Authorize(Roles = "BACKOFFICE,GRID_OPERATOR")]
    public async Task<IActionResult> CreateSlot([FromBody] CreateSlotDto request)
    {
        if (!ModelState.IsValid)
        {
            return BadRequest(ApiResponse<object>.ErrorResponse("Invalid request data."));
        }

        var slot = await _slotService.CreateSlotAsync(request);
        return Ok(ApiResponse<SlotDto>.SuccessResponse("Slot created successfully.", slot));
    }

    /// <summary>
    /// Updates a slot (Operator/Backoffice).
    /// </summary>
    [HttpPut("{id}")]
    [Authorize(Roles = "BACKOFFICE,GRID_OPERATOR")]
    public async Task<IActionResult> UpdateSlot(string id, [FromBody] UpdateSlotDto request)
    {
        if (!ModelState.IsValid)
        {
            return BadRequest(ApiResponse<object>.ErrorResponse("Invalid request data."));
        }

        var slot = await _slotService.UpdateSlotAsync(id, request);
        if (slot == null)
        {
            return NotFound(ApiResponse<object>.ErrorResponse("Slot not found."));
        }

        return Ok(ApiResponse<SlotDto>.SuccessResponse("Slot updated successfully.", slot));
    }

    /// <summary>
    /// Deletes a slot safely.
    /// </summary>
    [HttpDelete("{id}")]
    [Authorize(Roles = "BACKOFFICE,GRID_OPERATOR")]
    public async Task<IActionResult> DeleteSlot(string id)
    {
        try
        {
            var success = await _slotService.DeleteSlotAsync(id);
            if (!success)
            {
                return NotFound(ApiResponse<object>.ErrorResponse("Slot not found."));
            }
            return Ok(ApiResponse<object>.SuccessResponse("Slot deleted successfully."));
        }
        catch (InvalidOperationException ex)
        {
            return BadRequest(ApiResponse<object>.ErrorResponse(ex.Message));
        }
    }
}
