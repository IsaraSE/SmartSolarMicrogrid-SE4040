/*
 * File Name: IReservationService.cs
 * Project: Smart Solar Microgrid Trading System
 * Module: SE4040 Enterprise Application Development
 * Author: Isara
 * Description: Service interface for reservation logic including business rules.
 * Date: 2026-09-14
 */

using SmartSolarMicrogrid.Api.Models.DTOs;
using SmartSolarMicrogrid.Api.Models.Enums;

namespace SmartSolarMicrogrid.Api.Services.Reservations;

public interface IReservationService
{
    Task<IEnumerable<ReservationDto>> GetReservationsAsync(string? nic, string? stationId, string? status, DateTime? date);
    Task<ReservationDto?> GetReservationByIdAsync(string id);
    Task<(bool Success, string Message, ReservationDto? Reservation)> CreateReservationAsync(string prosumerNic, CreateReservationDto request);
    Task<(bool Success, string Message, ReservationDto? Reservation)> UpdateReservationAsync(string id, string prosumerNic, string role, UpdateReservationDto request);
    Task<(bool Success, string Message)> CancelReservationAsync(string id, string prosumerNic, string role);
    Task<(bool Success, string Message, ReservationDto? Reservation)> UpdateReservationStatusAsync(string id, ReservationStatus newStatus);
}
