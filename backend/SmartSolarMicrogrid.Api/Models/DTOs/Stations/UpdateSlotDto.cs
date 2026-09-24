/*
 * File Name: UpdateSlotDto.cs
 * Project: Smart Solar Microgrid Trading System
 * Module: SE4040 Enterprise Application Development
 * Author: IT22154880
 * Description: Implementation of UpdateSlotDto.cs
 * Date: 2026-09-19
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

    [Required]
    [Range(0.1, 1000)]
    public double Capacity { get; set; }

    [MaxLength(500)]
    public string? Notes { get; set; }
}
