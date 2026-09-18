/*
 * File Name: ProsumerService.cs
 * Project: Smart Solar Microgrid Trading System
 * Module: SE4040 Enterprise Application Development
 * Author: Isara
 * Description: Service implementation for prosumer administration logic.
 * Date: 2026-09-14
 */

using SmartSolarMicrogrid.Api.Models.DTOs;
using SmartSolarMicrogrid.Api.Models.Entities;
using SmartSolarMicrogrid.Api.Models.Enums;
using SmartSolarMicrogrid.Api.Repositories;

namespace SmartSolarMicrogrid.Api.Services.Users;

public class ProsumerService : IProsumerService
{
    private readonly IUserDetailsRepository _userRepository;

    public ProsumerService(IUserDetailsRepository userRepository)
    {
        _userRepository = userRepository;
    }

    public async Task<IEnumerable<UserDto>> GetAllProsumersAsync()
    {
        var users = await _userRepository.FindAsync(u => u.Role == UserRole.PROSUMER);
        return users.Select(MapToDto);
    }

    public async Task<UserDto?> GetProsumerByNicAsync(string nic)
    {
        var user = await _userRepository.GetByNicAsync(nic);
        if (user == null || user.Role != UserRole.PROSUMER) return null;
        return MapToDto(user);
    }

    public async Task<IEnumerable<UserDto>> GetPendingProsumersAsync()
    {
        var users = await _userRepository.FindAsync(u => u.Role == UserRole.PROSUMER && u.AccountStatus == AccountStatus.PENDING);
        return users.Select(MapToDto);
    }

    public async Task<UserDto?> ActivateProsumerAsync(string nic)
    {
        var user = await _userRepository.GetByNicAsync(nic);
        if (user == null || user.Role != UserRole.PROSUMER || user.AccountStatus != AccountStatus.PENDING)
        {
            return null;
        }

        user.AccountStatus = AccountStatus.ACTIVE;
        await _userRepository.UpdateAsync(user.UserId!, user);
        return MapToDto(user);
    }

    public async Task<IEnumerable<UserDto>> GetDeactivatedProsumersAsync()
    {
        var users = await _userRepository.FindAsync(u => u.Role == UserRole.PROSUMER && u.AccountStatus == AccountStatus.DEACTIVATED);
        return users.Select(MapToDto);
    }

    public async Task<UserDto?> ReactivateProsumerAsync(string nic)
    {
        var user = await _userRepository.GetByNicAsync(nic);
        if (user == null || user.Role != UserRole.PROSUMER || user.AccountStatus != AccountStatus.DEACTIVATED)
        {
            return null;
        }

        user.AccountStatus = AccountStatus.ACTIVE;
        await _userRepository.UpdateAsync(user.UserId!, user);
        return MapToDto(user);
    }

    public async Task<UserDto?> DeactivateProsumerAsync(string nic)
    {
        var user = await _userRepository.GetByNicAsync(nic);
        if (user == null || user.Role != UserRole.PROSUMER || user.AccountStatus != AccountStatus.ACTIVE)
        {
            return null;
        }

        user.AccountStatus = AccountStatus.DEACTIVATED;
        await _userRepository.UpdateAsync(user.UserId!, user);
        return MapToDto(user);
    }

    private static UserDto MapToDto(UserDetail user)
    {
        return new UserDto
        {
            UserId = user.UserId!,
            Nic = user.Nic,
            FullName = user.FullName,
            Email = user.Email,
            Phone = user.Phone,
            Role = user.Role,
            AccountStatus = user.AccountStatus,
            Address = user.Address,
            CreatedAt = user.CreatedAt
        };
    }
}
