/*
 * File Name: DashboardController.cs
 * Project: Smart Solar Microgrid Trading System
 * Module: SE4040 Enterprise Application Development
 * Author: IT22154880
 * Description: Implementation of DashboardController.cs
 * Date: 2026-09-22
 */

using System.Security.Claims;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using SmartSolarMicrogrid.Api.Models.DTOs;
using SmartSolarMicrogrid.Api.Services;

namespace SmartSolarMicrogrid.Api.Controllers.Dashboard;

[ApiController]
[Route("api/[controller]")]
[Authorize]
public class DashboardController : ControllerBase
{
    private readonly IDashboardService _dashboardService;

    public DashboardController(IDashboardService dashboardService)
    {
        _dashboardService = dashboardService;
    }

    /// <summary>
    /// Gets dashboard statistics. Internally filters based on calling user's role.
    /// </summary>
    [HttpGet]
    public async Task<IActionResult> GetDashboardStats()
    {
        // Retrieves dashboard stats data from the system.
        var role = User.FindFirstValue(ClaimTypes.Role) ?? string.Empty;
        var userNic = User.FindFirstValue("nic");

        var stats = await _dashboardService.GetStatsAsync(role, userNic);

        return Ok(ApiResponse<DashboardStatsDto>.SuccessResponse("Dashboard stats retrieved successfully.", stats));
    }
}
