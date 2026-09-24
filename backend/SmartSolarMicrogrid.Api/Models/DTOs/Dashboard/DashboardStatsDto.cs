/*
 * File Name: DashboardStatsDto.cs
 * Project: Smart Solar Microgrid Trading System
 * Module: SE4040 Enterprise Application Development
 * Author: IT22154880
 * Description: Implementation of DashboardStatsDto.cs
 * Date: 2026-09-22
 */

namespace SmartSolarMicrogrid.Api.Models.DTOs.Dashboard;

public class DashboardStatsDto
{
    public int PendingReservationsCount { get; set; }
    public int ApprovedFutureReservationsCount { get; set; }
}
