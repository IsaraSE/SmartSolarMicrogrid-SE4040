/*
 * File Name: UserService.cs
 * Project: Smart Solar Microgrid Trading System
 * Module: SE4040 Enterprise Application Development
 * Author: Isara
 * Description: Service implementation for user management logic.
 * Date: 2026-09-14
 */

using SmartSolarMicrogrid.Api.Exceptions;
using SmartSolarMicrogrid.Api.Models.DTOs;
using SmartSolarMicrogrid.Api.Models.Entities;
using SmartSolarMicrogrid.Api.Models.Enums;
using SmartSolarMicrogrid.Api.Repositories;

namespace SmartSolarMicrogrid.Api.Services.Users;

public class UserService : IUserService
{
    private readonly IUserDetailsRepository _userRepository;

    public UserService(IUserDetailsRepository userRepository)
    {
        _userRepository = userRepository;
    }

    public async Task<IEnumerable<UserDto>> GetAllNonProsumersAsync()
    {
        var users = await _userRepository.FindAsync(u => u.Role != UserRole.PROSUMER);
        return users.Select(MapToDto);
    }

    public async Task<UserDto?> GetUserByIdAsync(string userId)
    {
        var user = await _userRepository.GetByIdAsync(userId);
        return user != null ? MapToDto(user) : null;
    }

    public async Task<UserDto> CreateUserAsync(CreateUserDto request)
    {
        var errors = new Dictionary<string, string[]>();

        var existingUser = await _userRepository.GetByEmailAsync(request.Email);
        if (existingUser != null)
        {
            errors.Add("Email", new[] { "A user with this email already exists." });
        }

        var existingPhone = await _userRepository.GetByPhoneAsync(request.Phone);
        if (existingPhone != null)
        {
            errors.Add("Phone", new[] { "A user with this phone number already exists." });
        }

        if (errors.Any())
        {
            throw new AppValidationException(errors);
        }

        var user = new UserDetail
        {
            UserId = MongoDB.Bson.ObjectId.GenerateNewId().ToString(),
            FullName = request.FullName,
            Email = request.Email,
            Phone = request.Phone,
            PasswordHash = BCrypt.Net.BCrypt.HashPassword(request.Password),
            Role = request.Role,
            AccountStatus = AccountStatus.ACTIVE,
            Address = request.Address,
            AdditionalInfo = request.AdditionalInfo,
            CreatedAt = DateTime.UtcNow
        };

        await _userRepository.CreateAsync(user);
        return MapToDto(user);
    }

    public async Task<UserDto?> UpdateUserAsync(string userId, UpdateUserDto request)
    {
        var user = await _userRepository.GetByIdAsync(userId);
        if (user == null)
        {
            return null;
        }

        var errors = new Dictionary<string, string[]>();

        var existingPhone = await _userRepository.GetByPhoneAsync(request.Phone);
        if (existingPhone != null && existingPhone.UserId != userId)
        {
            errors.Add("Phone", new[] { "A user with this phone number already exists." });
        }

        if (errors.Any())
        {
            throw new AppValidationException(errors);
        }

        user.FullName = request.FullName;
        user.Phone = request.Phone;
        user.Address = request.Address;
        user.AdditionalInfo = request.AdditionalInfo;
        user.AccountStatus = request.AccountStatus;

        await _userRepository.UpdateAsync(userId, user);
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
            AdditionalInfo = user.AdditionalInfo,
            CreatedAt = user.CreatedAt
        };
    }
}
