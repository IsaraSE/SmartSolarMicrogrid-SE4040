/*
 * File Name: UsersController.cs
 * Project: Smart Solar Microgrid Trading System
 * Module: SE4040 Enterprise Application Development
 * Author: Isara
 * Description: Controller for Backoffice user management.
 * Date: 2026-09-14
 */

using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using SmartSolarMicrogrid.Api.Models.DTOs;
using SmartSolarMicrogrid.Api.Services;

namespace SmartSolarMicrogrid.Api.Controllers.Users;

[ApiController]
[Route("api/[controller]")]
[Authorize(Roles = "BACKOFFICE")]
public class UsersController : ControllerBase
{
    private readonly IUserService _userService;

    public UsersController(IUserService userService)
    {
        _userService = userService;
    }

    /// <summary>
    /// Gets all users excluding prosumers.
    /// </summary>
    [HttpGet]
    public async Task<IActionResult> GetAllUsers()
    {
        var users = await _userService.GetAllNonProsumersAsync();
        return Ok(ApiResponse<IEnumerable<UserDto>>.SuccessResponse("Users retrieved successfully.", users));
    }

    /// <summary>
    /// Gets a user by ID.
    /// </summary>
    [HttpGet("{id}")]
    public async Task<IActionResult> GetUserById(string id)
    {
        var user = await _userService.GetUserByIdAsync(id);
        if (user == null)
        {
            return NotFound(ApiResponse<object>.ErrorResponse("User not found."));
        }
        return Ok(ApiResponse<UserDto>.SuccessResponse("User retrieved successfully.", user));
    }

    /// <summary>
    /// Creates a new backoffice or grid operator user.
    /// </summary>
    [HttpPost]
    public async Task<IActionResult> CreateUser([FromBody] CreateUserDto request)
    {
        if (!ModelState.IsValid)
        {
            return BadRequest(ApiResponse<object>.ErrorResponse("Invalid request data."));
        }

        var user = await _userService.CreateUserAsync(request);
        return CreatedAtAction(nameof(GetUserById), new { id = user.UserId }, ApiResponse<UserDto>.SuccessResponse("User created successfully.", user));
    }

    /// <summary>
    /// Updates an existing user.
    /// </summary>
    [HttpPut("{id}")]
    public async Task<IActionResult> UpdateUser(string id, [FromBody] UpdateUserDto request)
    {
        if (!ModelState.IsValid)
        {
            return BadRequest(ApiResponse<object>.ErrorResponse("Invalid request data."));
        }

        var updatedUser = await _userService.UpdateUserAsync(id, request);
        if (updatedUser == null)
        {
            return NotFound(ApiResponse<object>.ErrorResponse("User not found."));
        }

        return Ok(ApiResponse<UserDto>.SuccessResponse("User updated successfully.", updatedUser));
    }
}
