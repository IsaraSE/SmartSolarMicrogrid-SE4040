/*
 * File Name: UpdateStationDto.cs
 * Project: Smart Solar Microgrid Trading System
 * Module: SE4040 Enterprise Application Development
 * Author: IT22154880
 * Description: Implementation of UpdateStationDto.cs
 * Date: 2026-09-22
 */

using System.ComponentModel.DataAnnotations;
using SmartSolarMicrogrid.Api.Models.Enums;

namespace SmartSolarMicrogrid.Api.Models.DTOs.Stations;

public class UpdateStationDto
{
    [Required]
    [MaxLength(100)]
    public string StationName { get; set; } = null!;

    [Required]
    [MaxLength(255)]
    public string Address { get; set; } = null!;
    
    public double Latitude { get; set; }
    public double Longitude { get; set; }

    [Required]
    [Range(0, double.MaxValue)]
    public double Capacity { get; set; }

    [Required]
    [Range(0, 100)]
    public int BatterySlotCount { get; set; }

    [Required]
    public string OperatingStartTime { get; set; } = null!;

    [Required]
    public string OperatingEndTime { get; set; } = null!;

    [Required]
    public StationStatus Status { get; set; }

    [MaxLength(500)]
    public string? Description { get; set; }
}
