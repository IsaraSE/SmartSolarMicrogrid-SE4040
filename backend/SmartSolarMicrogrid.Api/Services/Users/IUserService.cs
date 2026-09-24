/*
 * File Name: IUserService.cs
 * Project: Smart Solar Microgrid Trading System
 * Module: SE4040 Enterprise Application Development
 * Author: IT22194862
 * Description: Implementation of IUserService.cs
 * Date: 2026-09-23
 */

using SmartSolarMicrogrid.Api.Models.DTOs;
using SmartSolarMicrogrid.Api.Models.DTOs.Users;

namespace SmartSolarMicrogrid.Api.Services.Users;

public interface IUserService
{
    Task<IEnumerable<UserDto>> GetAllNonProsumersAsync();
    Task<UserDto?> GetUserByIdAsync(string userId);
    Task<UserDto> CreateUserAsync(CreateUserDto request);
    Task<UserDto?> UpdateUserAsync(string userId, UpdateUserDto request);
    Task<UserDto?> UpdateProfileAsync(string userId, UpdateProfileDto request);
    Task<bool> ChangePasswordAsync(string userId, ChangePasswordDto request);
}
