/*
 * File Name: IProsumerService.cs
 * Project: Smart Solar Microgrid Trading System
 * Module: SE4040 Enterprise Application Development
 * Author: IT22194862
 * Description: Implementation of IProsumerService.cs
 * Date: 2026-09-16
 */

using SmartSolarMicrogrid.Api.Models.DTOs;

namespace SmartSolarMicrogrid.Api.Services.Users;

public interface IProsumerService
{
    Task<(bool Success, string Message, UserDto? Prosumer)> RegisterProsumerAsync(RegisterProsumerDto request);
    Task<(bool Success, string Message, UserDto? Prosumer)> UpdateProsumerProfileAsync(string nic, UpdateProsumerDto request);
    Task<IEnumerable<UserDto>> GetAllProsumersAsync();
    Task<UserDto?> GetProsumerByNicAsync(string nic);
    Task<IEnumerable<UserDto>> GetPendingProsumersAsync();
    Task<UserDto?> ActivateProsumerAsync(string nic);
    Task<IEnumerable<UserDto>> GetDeactivatedProsumersAsync();
    Task<UserDto?> ReactivateProsumerAsync(string nic);
    Task<(bool Success, string Message, UserDto? Prosumer)> DeactivateProsumerAsync(string nic);
}
