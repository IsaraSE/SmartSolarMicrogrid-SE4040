/*
 * File Name: IProsumerService.cs
 * Project: Smart Solar Microgrid Trading System
 * Module: SE4040 Enterprise Application Development
 * Author: Isara
 * Description: Service interface for prosumer administration logic.
 * Date: 2026-09-14
 */

using SmartSolarMicrogrid.Api.Models.DTOs;

namespace SmartSolarMicrogrid.Api.Services.Users;

public interface IProsumerService
{
    Task<IEnumerable<UserDto>> GetAllProsumersAsync();
    Task<UserDto?> GetProsumerByNicAsync(string nic);
    Task<IEnumerable<UserDto>> GetPendingProsumersAsync();
    Task<UserDto?> ActivateProsumerAsync(string nic);
    Task<IEnumerable<UserDto>> GetDeactivatedProsumersAsync();
    Task<UserDto?> ReactivateProsumerAsync(string nic);
    Task<UserDto?> DeactivateProsumerAsync(string nic);
}
