/*
 * File Name: UserService.cs
 * Project: Smart Solar Microgrid Trading System
 * Module: SE4040 Enterprise Application Development
 * Author: IT22194862
 * Description: Implementation of UserService.cs
 * Date: 2026-09-17
 */

using SmartSolarMicrogrid.Api.Exceptions;
using SmartSolarMicrogrid.Api.Models.DTOs;
using SmartSolarMicrogrid.Api.Models.DTOs.Users;
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
        // Retrieves all non prosumers data from the system.
        var users = await _userRepository.FindAsync(u => u.Role != UserRole.PROSUMER);
        return users.Select(MapToDto);
    }

    public async Task<UserDto?> GetUserByIdAsync(string userId)
    {
        // Retrieves user by id data from the system.
        var user = await _userRepository.GetByIdAsync(userId);
        return user != null ? MapToDto(user) : null;
    }

    public async Task<UserDto> CreateUserAsync(CreateUserDto request)
    {
        // Handles the creation of user.
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
        // Updates existing user records.
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

    private UserDto MapToDto(UserDetail user)
    {
        // Maps to dto to the corresponding DTO.
        return new UserDto
        {
            UserId = user.UserId,
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

    public async Task<UserDto?> UpdateProfileAsync(string userId, UpdateProfileDto request)
    {
        // Updates existing profile records.
        var user = await _userRepository.GetByIdAsync(userId);
        if (user == null)
        {
            return null;
        }

        var errors = new Dictionary<string, string[]>();

        var existingEmail = await _userRepository.GetByEmailAsync(request.Email);
        if (existingEmail != null && existingEmail.UserId != userId)
        {
            errors.Add("Email", new[] { "This email is already taken by another account." });
        }

        var existingPhone = await _userRepository.GetByPhoneAsync(request.Phone);
        if (existingPhone != null && existingPhone.UserId != userId)
        {
            errors.Add("Phone", new[] { $"This phone number is already taken by another account. (Existing: {existingPhone.UserId ?? "null"}, Current: {userId})" });
        }

        if (errors.Any())
        {
            throw new AppValidationException(errors);
        }

        user.FullName = request.FullName;
        user.Email = request.Email;
        user.Phone = request.Phone;
        user.Address = request.Address;

        await _userRepository.UpdateAsync(userId, user);
        return MapToDto(user);
    }

    public async Task<bool> ChangePasswordAsync(string userId, ChangePasswordDto request)
    {
        // Executes logic to change password.
        var user = await _userRepository.GetByIdAsync(userId);
        if (user == null)
        {
            return false;
        }

        bool isPasswordValid = BCrypt.Net.BCrypt.Verify(request.CurrentPassword, user.PasswordHash);
        if (!isPasswordValid)
        {
            throw new AppValidationException(new Dictionary<string, string[]>
            {
                { "CurrentPassword", new[] { "Current password is incorrect." } }
            });
        }

        user.PasswordHash = BCrypt.Net.BCrypt.HashPassword(request.NewPassword);
        await _userRepository.UpdateAsync(userId, user);

        return true;
    }
}
