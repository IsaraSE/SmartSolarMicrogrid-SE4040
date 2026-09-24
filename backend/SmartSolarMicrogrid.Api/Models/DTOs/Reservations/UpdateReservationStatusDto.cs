/*
 * File Name: UpdateReservationStatusDto.cs
 * Project: Smart Solar Microgrid Trading System
 * Module: SE4040 Enterprise Application Development
 * Author: IT22194862
 * Description: Implementation of UpdateReservationStatusDto.cs
 * Date: 2026-09-19
 */

using System.ComponentModel.DataAnnotations;
using SmartSolarMicrogrid.Api.Models.Enums;

namespace SmartSolarMicrogrid.Api.Models.DTOs.Reservations;

public class UpdateReservationStatusDto
{
    [Required]
    public ReservationStatus Status { get; set; }
}
