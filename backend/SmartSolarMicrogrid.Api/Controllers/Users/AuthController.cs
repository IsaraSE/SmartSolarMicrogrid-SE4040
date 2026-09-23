/*
 * File Name: AuthController.cs
 * Project: Smart Solar Microgrid Trading System
 * Module: SE4040 Enterprise Application Development
 * Author: Isara
 * Description: Controller for authentication operations.
 * Date: 2026-09-14
 */

using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using SmartSolarMicrogrid.Api.Models.DTOs;
using SmartSolarMicrogrid.Api.Models.DTOs.Users;
using SmartSolarMicrogrid.Api.Services;
using SmartSolarMicrogrid.Api.Services.Users;
using System.Security.Claims;

namespace SmartSolarMicrogrid.Api.Controllers.Users;

[ApiController]
[Route("api/[controller]")]
public class AuthController : ControllerBase
{
    private readonly IAuthService _authService;
    private readonly IUserService _userService;

    public AuthController(IAuthService authService, IUserService userService)
    {
        _authService = authService;
        _userService = userService;
    }

    /// <summary>
    /// Authenticates a Backoffice or Grid Operator user and returns a JWT.
    /// </summary>
    [HttpPost("login")]
    public async Task<IActionResult> Login([FromBody] LoginRequestDto request)
    {
        if (!ModelState.IsValid)
        {
            return BadRequest(ApiResponse<object>.ErrorResponse("Invalid request format."));
        }

        var (success, message, data) = await _authService.LoginAsync(request);

        if (!success)
        {
            return Unauthorized(ApiResponse<object>.ErrorResponse(message));
        }

        return Ok(ApiResponse<LoginResponseDto>.SuccessResponse(message, data));
    }

    /// <summary>
    /// Gets the authenticated user's profile details.
    /// </summary>
    [HttpGet("profile")]
    [Authorize]
    public async Task<IActionResult> GetProfile()
    {
        var userId = User.FindFirst(ClaimTypes.NameIdentifier)?.Value;
        if (string.IsNullOrEmpty(userId))
        {
            return Unauthorized(ApiResponse<object>.ErrorResponse("Unauthorized."));
        }

        var user = await _userService.GetUserByIdAsync(userId);
        if (user == null)
        {
            return NotFound(ApiResponse<object>.ErrorResponse("User not found."));
        }

        return Ok(ApiResponse<UserDto>.SuccessResponse("Profile retrieved successfully.", user));
    }

    /// <summary>
    /// Updates the authenticated user's profile details.
    /// </summary>
    [HttpPut("profile")]
    [Authorize]
    public async Task<IActionResult> UpdateProfile([FromBody] UpdateProfileDto request)
    {
        if (!ModelState.IsValid)
        {
            return BadRequest(ApiResponse<object>.ErrorResponse("Invalid request format."));
        }

        var userId = User.FindFirst(ClaimTypes.NameIdentifier)?.Value;
        if (string.IsNullOrEmpty(userId))
        {
            return Unauthorized(ApiResponse<object>.ErrorResponse("Unauthorized."));
        }

        try
        {
            var updatedUser = await _userService.UpdateProfileAsync(userId, request);
            if (updatedUser == null)
            {
                return NotFound(ApiResponse<object>.ErrorResponse("User not found."));
            }
            return Ok(ApiResponse<UserDto>.SuccessResponse("Profile updated successfully.", updatedUser));
        }
        catch (SmartSolarMicrogrid.Api.Exceptions.AppValidationException ex)
        {
            return BadRequest(ApiResponse<object>.ErrorResponse(string.Join(" ", ex.Errors.Values.SelectMany(v => v))));
        }
    }

    /// <summary>
    /// Changes the authenticated user's password.
    /// </summary>
    [HttpPut("password")]
    [Authorize]
    public async Task<IActionResult> ChangePassword([FromBody] ChangePasswordDto request)
    {
        if (!ModelState.IsValid)
        {
            return BadRequest(ApiResponse<object>.ErrorResponse("Invalid request format."));
        }

        var userId = User.FindFirst(ClaimTypes.NameIdentifier)?.Value;
        if (string.IsNullOrEmpty(userId))
        {
            return Unauthorized(ApiResponse<object>.ErrorResponse("Unauthorized."));
        }

        try
        {
            var result = await _userService.ChangePasswordAsync(userId, request);
            if (!result)
            {
                return NotFound(ApiResponse<object>.ErrorResponse("User not found."));
            }
            return Ok(ApiResponse<object>.SuccessResponse("Password updated successfully."));
        }
        catch (SmartSolarMicrogrid.Api.Exceptions.AppValidationException ex)
        {
            return BadRequest(ApiResponse<object>.ErrorResponse(string.Join(" ", ex.Errors.Values.SelectMany(v => v))));
        }
    }
}
