/*
 * File Name: AuthController.cs
 * Project: Smart Solar Microgrid Trading System
 * Module: SE4040 Enterprise Application Development
 * Author: Isara
 * Description: Controller for authentication operations.
 * Date: 2026-09-14
 */

using Microsoft.AspNetCore.Mvc;
using SmartSolarMicrogrid.Api.Models.DTOs;
using SmartSolarMicrogrid.Api.Services;

namespace SmartSolarMicrogrid.Api.Controllers.Users;

[ApiController]
[Route("api/[controller]")]
public class AuthController : ControllerBase
{
    private readonly IAuthService _authService;

    public AuthController(IAuthService authService)
    {
        _authService = authService;
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

        var response = await _authService.LoginAsync(request);

        if (response == null)
        {
            return Unauthorized(ApiResponse<object>.ErrorResponse("Invalid email or password, or account is deactivated."));
        }

        return Ok(ApiResponse<LoginResponseDto>.SuccessResponse("Login successful", response));
    }
}
