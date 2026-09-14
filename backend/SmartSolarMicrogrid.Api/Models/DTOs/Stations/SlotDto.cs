/*
 * File Name: SlotDto.cs
 * Project: Smart Solar Microgrid Trading System
 * Module: SE4040 Enterprise Application Development
 * Author: Isara
 * Description: Data transfer object for slot response.
 * Date: 2026-09-14
 */

using SmartSolarMicrogrid.Api.Models.Enums;

namespace SmartSolarMicrogrid.Api.Models.DTOs.Stations;

public class SlotDto
{
    public string SlotId { get; set; } = null!;
    public string StationId { get; set; } = null!;
    public DateTime StartDateTime { get; set; }
    public DateTime EndDateTime { get; set; }
    public SlotStatus Status { get; set; }
}
