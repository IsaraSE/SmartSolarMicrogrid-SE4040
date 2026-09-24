/*
 * File Name: DashboardService.cs
 * Project: Smart Solar Microgrid Trading System
 * Module: SE4040 Enterprise Application Development
 * Author: IT22154880
 * Description: Implementation of DashboardService.cs
 * Date: 2026-09-19
 */

using SmartSolarMicrogrid.Api.Models.DTOs;
using SmartSolarMicrogrid.Api.Models.Enums;
using SmartSolarMicrogrid.Api.Repositories;

namespace SmartSolarMicrogrid.Api.Services.Dashboard;

public class DashboardService : IDashboardService
{
    private readonly IEnergyReservationRepository _reservationRepository;

    public DashboardService(IEnergyReservationRepository reservationRepository)
    {
        _reservationRepository = reservationRepository;
    }

    public async Task<DashboardStatsDto> GetStatsAsync(string role, string? userNic)
    {
        // Retrieves stats data from the system.
        var allReservations = await _reservationRepository.GetAllAsync();
        
        var query = allReservations.AsEnumerable();

        if (role == "PROSUMER" && !string.IsNullOrEmpty(userNic))
        {
            query = query.Where(r => r.ProsumerNic == userNic);
        }

        var pendingCount = query.Count(r => r.Status == ReservationStatus.PENDING);
        var approvedFutureCount = query.Count(r => r.Status == ReservationStatus.APPROVED && r.ScheduledStartDateTime.Date >= DateTime.UtcNow.Date);

        return new DashboardStatsDto
        {
            PendingReservationsCount = pendingCount,
            ApprovedFutureReservationsCount = approvedFutureCount
        };
    }
}
