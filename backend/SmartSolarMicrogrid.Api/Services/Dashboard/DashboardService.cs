/*
 * File Name: DashboardService.cs
 * Project: Smart Solar Microgrid Trading System
 * Module: SE4040 Enterprise Application Development
 * Author: Isara
 * Description: Service implementation for dashboard metrics based on role.
 * Date: 2026-09-14
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
