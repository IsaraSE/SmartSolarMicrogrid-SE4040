/*
 * File Name: CreateSlotDto.cs
 * Project: Smart Solar Microgrid Trading System
 * Module: SE4040 Enterprise Application Development
 * Author: Isara
 * Description: Data transfer object for creating a slot.
 * Date: 2026-09-14
 */

using System.ComponentModel.DataAnnotations;

namespace SmartSolarMicrogrid.Api.Models.DTOs.Stations;

public class CreateSlotDto
{
    [Required]
    public string StationId { get; set; } = null!;

    [Required]
    public string SlotName { get; set; } = null!;

    [Required]
    public DateTime StartDateTime { get; set; }

    [Required]
    public DateTime EndDateTime { get; set; }

    [MaxLength(500)]
    public string? Notes { get; set; }
}
