/*
 * File Name: StationDto.cs
 * Project: Smart Solar Microgrid Trading System
 * Module: SE4040 Enterprise Application Development
 * Author: IT22154880
 * Description: Implementation of StationDto.cs
 * Date: 2026-09-20
 */

using SmartSolarMicrogrid.Api.Models.Enums;

namespace SmartSolarMicrogrid.Api.Models.DTOs.Stations;

public class StationDto
{
    public string StationId { get; set; } = null!;
    public string StationName { get; set; } = null!;
    public string Address { get; set; } = null!;
    public double Latitude { get; set; }
    public double Longitude { get; set; }
    public double Capacity { get; set; }
    public int BatterySlotCount { get; set; }
    public string OperatingStartTime { get; set; } = null!;
    public string OperatingEndTime { get; set; } = null!;
    public string? Description { get; set; }
    public StationStatus Status { get; set; }
}
