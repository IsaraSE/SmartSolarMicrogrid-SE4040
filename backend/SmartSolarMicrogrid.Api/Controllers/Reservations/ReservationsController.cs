/*
 * File Name: ReservationsController.cs
 * Project: Smart Solar Microgrid Trading System
 * Module: SE4040 Enterprise Application Development
 * Author: Isara
 * Description: Controller for shared web/mobile reservation management.
 * Date: 2026-09-14
 */

using System.Security.Claims;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using SmartSolarMicrogrid.Api.Models.DTOs;
using SmartSolarMicrogrid.Api.Services;

namespace SmartSolarMicrogrid.Api.Controllers.Reservations;

[ApiController]
[Route("api/[controller]")]
[Authorize]
public class ReservationsController : ControllerBase
{
    private readonly IReservationService _reservationService;

    public ReservationsController(IReservationService reservationService)
    {
        _reservationService = reservationService;
    }

    /// <summary>
    /// Gets reservations based on role and filters.
    /// </summary>
    [HttpGet]
    [HttpGet("search")]
    public async Task<IActionResult> GetReservations([FromQuery] string? nic, [FromQuery] string? stationId, [FromQuery] string? status, [FromQuery] DateTime? date)
    {
        var role = User.FindFirstValue(ClaimTypes.Role);
        var userNic = User.FindFirstValue("nic");

        // If prosumer, force filter by their own NIC
        if (role == "PROSUMER")
        {
            nic = userNic;
        }

        var reservations = await _reservationService.GetReservationsAsync(nic, stationId, status, date);
        return Ok(ApiResponse<IEnumerable<ReservationDto>>.SuccessResponse("Reservations retrieved successfully.", reservations));
    }

    /// <summary>
    /// Gets a prosumer's current (approved, upcoming) bookings.
    /// </summary>
    [HttpGet("current/{prosumerNic}")]
    [Authorize(Roles = "PROSUMER,BACKOFFICE,GRID_OPERATOR")]
    public async Task<IActionResult> GetCurrentReservations(string prosumerNic)
    {
        if (!IsOwnNicOrPrivileged(prosumerNic))
        {
            return Forbid();
        }

        var reservations = await _reservationService.GetCurrentReservationsByNicAsync(prosumerNic);
        return Ok(ApiResponse<IEnumerable<ReservationDto>>.SuccessResponse("Current reservations retrieved successfully.", reservations));
    }

    /// <summary>
    /// Gets a prosumer's pending bookings awaiting approval.
    /// </summary>
    [HttpGet("pending/{prosumerNic}")]
    [Authorize(Roles = "PROSUMER,BACKOFFICE,GRID_OPERATOR")]
    public async Task<IActionResult> GetPendingReservations(string prosumerNic)
    {
        if (!IsOwnNicOrPrivileged(prosumerNic))
        {
            return Forbid();
        }

        var reservations = await _reservationService.GetPendingReservationsByNicAsync(prosumerNic);
        return Ok(ApiResponse<IEnumerable<ReservationDto>>.SuccessResponse("Pending reservations retrieved successfully.", reservations));
    }

    /// <summary>
    /// Gets a prosumer's booking history (completed, cancelled, or past).
    /// </summary>
    [HttpGet("history/{prosumerNic}")]
    [Authorize(Roles = "PROSUMER,BACKOFFICE,GRID_OPERATOR")]
    public async Task<IActionResult> GetReservationHistory(string prosumerNic)
    {
        if (!IsOwnNicOrPrivileged(prosumerNic))
        {
            return Forbid();
        }

        var reservations = await _reservationService.GetHistoryReservationsByNicAsync(prosumerNic);
        return Ok(ApiResponse<IEnumerable<ReservationDto>>.SuccessResponse("Reservation history retrieved successfully.", reservations));
    }

    /// <summary>
    /// Ensures a PROSUMER can only read their own bookings, while Backoffice/Operator can read any.
    /// </summary>
    private bool IsOwnNicOrPrivileged(string prosumerNic)
    {
        var role = User.FindFirstValue(ClaimTypes.Role);
        if (role == "BACKOFFICE" || role == "GRID_OPERATOR")
        {
            return true;
        }
        var userNic = User.FindFirstValue("nic");
        return !string.IsNullOrEmpty(userNic) && userNic == prosumerNic;
    }

    /// <summary>
    /// Gets a specific reservation by ID.
    /// </summary>
    [HttpGet("{id}")]
    public async Task<IActionResult> GetReservationById(string id)
    {
        var reservation = await _reservationService.GetReservationByIdAsync(id);
        if (reservation == null)
        {
            return NotFound(ApiResponse<object>.ErrorResponse("Reservation not found."));
        }
        
        var role = User.FindFirstValue(ClaimTypes.Role);
        var userNic = User.FindFirstValue("nic");
        
        // Authorization check for Prosumer
        if (role == "PROSUMER" && reservation.ProsumerNic != userNic)
        {
            return Forbid();
        }

        return Ok(ApiResponse<ReservationDto>.SuccessResponse("Reservation retrieved successfully.", reservation));
    }

    /// <summary>
    /// Creates a new reservation. Prosumers use their own token. Admins must supply ProsumerNic.
    /// </summary>
    [HttpPost]
    [Authorize(Roles = "PROSUMER,BACKOFFICE,GRID_OPERATOR")]
    public async Task<IActionResult> CreateReservation([FromBody] CreateReservationDto request)
    {
        if (!ModelState.IsValid)
        {
            return BadRequest(ApiResponse<object>.ErrorResponse("Invalid request data."));
        }

        var role = User.FindFirstValue(ClaimTypes.Role);
        var userNic = User.FindFirstValue("nic");

        string targetNic;

        if (role == "PROSUMER")
        {
            if (string.IsNullOrEmpty(userNic))
                return Unauthorized(ApiResponse<object>.ErrorResponse("User NIC not found in token."));
            targetNic = userNic;
        }
        else
        {
            if (string.IsNullOrEmpty(request.ProsumerNic))
                return BadRequest(ApiResponse<object>.ErrorResponse("ProsumerNic is required when an Admin creates a reservation."));
            targetNic = request.ProsumerNic;
        }

        var (success, message, reservation) = await _reservationService.CreateReservationAsync(targetNic, request);
        
        if (!success)
        {
            return BadRequest(ApiResponse<object>.ErrorResponse(message));
        }

        return CreatedAtAction(nameof(GetReservationById), new { id = reservation!.ReservationId }, ApiResponse<ReservationDto>.SuccessResponse(message, reservation));
    }

    /// <summary>
    /// Updates a reservation. Prosumers can only update their own. Admins can update any.
    /// </summary>
    [HttpPut("{id}")]
    [Authorize(Roles = "PROSUMER,GRID_OPERATOR,BACKOFFICE")]
    public async Task<IActionResult> UpdateReservation(string id, [FromBody] UpdateReservationDto request)
    {
        if (!ModelState.IsValid)
        {
            return BadRequest(ApiResponse<object>.ErrorResponse("Invalid request data."));
        }

        var userNic = User.FindFirstValue("nic") ?? "";
        var role = User.FindFirstValue(ClaimTypes.Role) ?? "";
        if (role == "PROSUMER" && string.IsNullOrEmpty(userNic))
        {
            return Unauthorized(ApiResponse<object>.ErrorResponse("User NIC not found in token."));
        }

        var (success, message, reservation) = await _reservationService.UpdateReservationAsync(id, userNic, role, request);
        
        if (!success)
        {
            return BadRequest(ApiResponse<object>.ErrorResponse(message));
        }

        return Ok(ApiResponse<ReservationDto>.SuccessResponse(message, reservation));
    }

    /// <summary>
    /// Cancels a reservation. Prosumers can only cancel their own.
    /// </summary>
    [HttpPut("{id}/cancel")]
    [Authorize(Roles = "PROSUMER,GRID_OPERATOR")]
    public async Task<IActionResult> CancelReservation(string id)
    {
        var userNic = User.FindFirstValue("nic") ?? "";
        var role = User.FindFirstValue(ClaimTypes.Role) ?? "";
        if (role == "PROSUMER" && string.IsNullOrEmpty(userNic))
        {
            return Unauthorized(ApiResponse<object>.ErrorResponse("User NIC not found in token."));
        }

        var (success, message) = await _reservationService.CancelReservationAsync(id, userNic, role);
        
        if (!success)
        {
            return BadRequest(ApiResponse<object>.ErrorResponse(message));
        }

        return Ok(ApiResponse<object>.SuccessResponse(message));
    }

    /// <summary>
    /// Updates the status of a reservation (Operator).
    /// </summary>
    [HttpPut("{id}/status")]
    [Authorize(Roles = "GRID_OPERATOR")]
    public async Task<IActionResult> UpdateReservationStatus(string id, [FromBody] UpdateReservationStatusDto request)
    {
        if (!ModelState.IsValid)
        {
            return BadRequest(ApiResponse<object>.ErrorResponse("Invalid request data."));
        }

        var (success, message, reservation) = await _reservationService.UpdateReservationStatusAsync(id, request.Status);
        
        if (!success)
        {
            return BadRequest(ApiResponse<object>.ErrorResponse(message));
        }

        return Ok(ApiResponse<ReservationDto>.SuccessResponse(message, reservation));
    }

    [HttpPut("{id}/complete")]
    [Authorize(Roles = "GRID_OPERATOR,BACKOFFICE")]
    public async Task<IActionResult> CompleteReservation(string id)
    {
        var (success, message, reservation) = await _reservationService.UpdateReservationStatusAsync(id, Models.Enums.Reservations.ReservationStatus.COMPLETED);
        
        if (!success)
        {
            return BadRequest(ApiResponse<object>.ErrorResponse(message));
        }

        return Ok(ApiResponse<ReservationDto>.SuccessResponse("Session completed successfully.", reservation));
    }
}
