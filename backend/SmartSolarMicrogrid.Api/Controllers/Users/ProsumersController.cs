/*
 * File Name: ProsumersController.cs
 * Project: Smart Solar Microgrid Trading System
 * Module: SE4040 Enterprise Application Development
 * Author: Isara
 * Description: Controller for prosumer administration operations.
 * Date: 2026-09-14
 */

using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using SmartSolarMicrogrid.Api.Models.DTOs;
using SmartSolarMicrogrid.Api.Services;

namespace SmartSolarMicrogrid.Api.Controllers.Users;

[ApiController]
[Route("api/[controller]")]
[Authorize]
public class ProsumersController : ControllerBase
{
    private readonly IProsumerService _prosumerService;

    public ProsumersController(IProsumerService prosumerService)
    {
        _prosumerService = prosumerService;
    }

    /// <summary>
    /// Registers a new prosumer from the mobile app (public, no authentication required).
    /// </summary>
    [HttpPost("register")]
    [AllowAnonymous]
    public async Task<IActionResult> RegisterProsumer([FromBody] RegisterProsumerDto request)
    {
        if (!ModelState.IsValid)
        {
            return BadRequest(ApiResponse<object>.ErrorResponse("Invalid request data."));
        }

        var (success, message, prosumer) = await _prosumerService.RegisterProsumerAsync(request);
        if (!success)
        {
            return BadRequest(ApiResponse<object>.ErrorResponse(message));
        }

        return CreatedAtAction(nameof(GetProsumerByNic), new { nic = prosumer!.Nic }, ApiResponse<UserDto>.SuccessResponse(message, prosumer));
    }

    /// <summary>
    /// Updates a prosumer's own profile details. Allowed for the prosumer themselves or Backoffice.
    /// </summary>
    [HttpPut("{nic}")]
    [Authorize(Roles = "PROSUMER,BACKOFFICE")]
    public async Task<IActionResult> UpdateProsumerProfile(string nic, [FromBody] UpdateProsumerDto request)
    {
        if (!ModelState.IsValid)
        {
            return BadRequest(ApiResponse<object>.ErrorResponse("Invalid request data."));
        }

        var (success, message, prosumer) = await _prosumerService.UpdateProsumerProfileAsync(nic, request);
        if (!success)
        {
            return BadRequest(ApiResponse<object>.ErrorResponse(message));
        }

        return Ok(ApiResponse<UserDto>.SuccessResponse(message, prosumer));
    }

    /// <summary>
    /// Gets all prosumers.
    /// </summary>
    [HttpGet]
    [Authorize(Roles = "BACKOFFICE")]
    public async Task<IActionResult> GetAllProsumers()
    {
        var prosumers = await _prosumerService.GetAllProsumersAsync();
        return Ok(ApiResponse<IEnumerable<UserDto>>.SuccessResponse("Prosumers retrieved successfully.", prosumers));
    }

    /// <summary>
    /// Gets pending prosumers.
    /// </summary>
    [HttpGet("pending")]
    [Authorize(Roles = "BACKOFFICE")]
    public async Task<IActionResult> GetPendingProsumers()
    {
        var prosumers = await _prosumerService.GetPendingProsumersAsync();
        return Ok(ApiResponse<IEnumerable<UserDto>>.SuccessResponse("Pending prosumers retrieved successfully.", prosumers));
    }

    /// <summary>
    /// Gets deactivated prosumers.
    /// </summary>
    [HttpGet("deactivated")]
    [Authorize(Roles = "BACKOFFICE")]
    public async Task<IActionResult> GetDeactivatedProsumers()
    {
        var prosumers = await _prosumerService.GetDeactivatedProsumersAsync();
        return Ok(ApiResponse<IEnumerable<UserDto>>.SuccessResponse("Deactivated prosumers retrieved successfully.", prosumers));
    }

    /// <summary>
    /// Gets a specific prosumer by NIC.
    /// </summary>
    [HttpGet("{nic}")]
    [Authorize(Roles = "PROSUMER,BACKOFFICE")]
    public async Task<IActionResult> GetProsumerByNic(string nic)
    {
        var prosumer = await _prosumerService.GetProsumerByNicAsync(nic);
        if (prosumer == null)
        {
            return NotFound(ApiResponse<object>.ErrorResponse("Prosumer not found."));
        }
        return Ok(ApiResponse<UserDto>.SuccessResponse("Prosumer retrieved successfully.", prosumer));
    }

    /// <summary>
    /// Activates a pending prosumer.
    /// </summary>
    [HttpPut("{nic}/activate")]
    [Authorize(Roles = "BACKOFFICE")]
    public async Task<IActionResult> ActivateProsumer(string nic)
    {
        var activatedProsumer = await _prosumerService.ActivateProsumerAsync(nic);
        if (activatedProsumer == null)
        {
            return BadRequest(ApiResponse<object>.ErrorResponse("Prosumer not found or not in pending status."));
        }
        return Ok(ApiResponse<UserDto>.SuccessResponse("Prosumer activated successfully.", activatedProsumer));
    }

    /// <summary>
    /// Reactivates a deactivated prosumer.
    /// </summary>
    [HttpPut("{nic}/reactivate")]
    [Authorize(Roles = "BACKOFFICE")]
    public async Task<IActionResult> ReactivateProsumer(string nic)
    {
        var reactivatedProsumer = await _prosumerService.ReactivateProsumerAsync(nic);
        if (reactivatedProsumer == null)
        {
            return BadRequest(ApiResponse<object>.ErrorResponse("Prosumer not found or not in deactivated status."));
        }
        return Ok(ApiResponse<UserDto>.SuccessResponse("Prosumer reactivated successfully.", reactivatedProsumer));
    }
    /// <summary>
    /// Deactivates an active prosumer.
    /// </summary>
    [HttpPut("{nic}/deactivate")]
    [Authorize(Roles = "PROSUMER,BACKOFFICE")]
    public async Task<IActionResult> DeactivateProsumer(string nic)
    {
        var deactivatedProsumer = await _prosumerService.DeactivateProsumerAsync(nic);
        if (deactivatedProsumer == null)
        {
            return BadRequest(ApiResponse<object>.ErrorResponse("Prosumer not found or not in active status."));
        }
        return Ok(ApiResponse<UserDto>.SuccessResponse("Prosumer deactivated successfully.", deactivatedProsumer));
    }
}
