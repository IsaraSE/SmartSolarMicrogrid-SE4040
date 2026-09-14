/*
 * File Name: DashboardStatsDto.cs
 * Project: Smart Solar Microgrid Trading System
 * Module: SE4040 Enterprise Application Development
 * Author: Isara
 * Description: Data transfer object for dashboard statistics.
 * Date: 2026-09-14
 */

namespace SmartSolarMicrogrid.Api.Models.DTOs.Dashboard;

public class DashboardStatsDto
{
    public int PendingReservationsCount { get; set; }
    public int ApprovedFutureReservationsCount { get; set; }
}
