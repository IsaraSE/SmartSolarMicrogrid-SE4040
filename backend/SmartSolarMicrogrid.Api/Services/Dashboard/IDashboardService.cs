/*
 * File Name: IDashboardService.cs
 * Project: Smart Solar Microgrid Trading System
 * Module: SE4040 Enterprise Application Development
 * Author: Isara
 * Description: Service interface for dashboard metrics.
 * Date: 2026-09-14
 */

using SmartSolarMicrogrid.Api.Models.DTOs;

namespace SmartSolarMicrogrid.Api.Services.Dashboard;

public interface IDashboardService
{
    Task<DashboardStatsDto> GetStatsAsync(string role, string? userNic);
}
