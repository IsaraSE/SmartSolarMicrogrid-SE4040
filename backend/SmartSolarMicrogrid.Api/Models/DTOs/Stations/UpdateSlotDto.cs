/*
 * File Name: UpdateSlotDto.cs
 * Project: Smart Solar Microgrid Trading System
 * Module: SE4040 Enterprise Application Development
 * Author: Isara
 * Description: Data transfer object for updating a slot.
 * Date: 2026-09-14
 */

using System.ComponentModel.DataAnnotations;
using SmartSolarMicrogrid.Api.Models.Enums;

namespace SmartSolarMicrogrid.Api.Models.DTOs.Stations;

public class UpdateSlotDto
{
    [Required]
    public DateTime StartDateTime { get; set; }

    [Required]
    public DateTime EndDateTime { get; set; }

    [Required]
    public SlotStatus Status { get; set; }

    [MaxLength(500)]
    public string? Notes { get; set; }
}
