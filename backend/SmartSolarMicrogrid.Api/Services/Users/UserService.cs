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

    /// <summary>
    /// Retrieves all staff users (BACKOFFICE and GRID_OPERATOR), excluding prosumers who are
    /// managed separately.
    /// </summary>
    /// <returns>The non-prosumer users mapped to DTOs.</returns>
    public async Task<IEnumerable<UserDto>> GetAllNonProsumersAsync()
    {
        var users = await _userRepository.FindAsync(u => u.Role != UserRole.PROSUMER);
        return users.Select(MapToDto);
    }

    /// <summary>
    /// Retrieves a single user by their identifier.
    /// </summary>
    /// <param name="userId">The user identifier.</param>
    /// <returns>The matching user DTO, or null if no user exists with the given id.</returns>
    public async Task<UserDto?> GetUserByIdAsync(string userId)
    {
        var user = await _userRepository.GetByIdAsync(userId);
        return user != null ? MapToDto(user) : null;
    }

    /// <summary>
    /// Creates a new staff user, ensuring the email and phone number are not already in use.
    /// </summary>
    /// <param name="request">The details of the user to create.</param>
    /// <returns>The newly created user as a DTO.</returns>
    /// <exception cref="AppValidationException">Thrown when the email and/or phone is already taken.</exception>
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

    /// <summary>
    /// Updates a staff user's profile fields and account status, ensuring the phone number
    /// is not already used by a different account.
    /// </summary>
    /// <param name="userId">The identifier of the user to update.</param>
    /// <param name="request">The updated user details.</param>
    /// <returns>The updated user DTO, or null if no user exists with the given id.</returns>
    /// <exception cref="AppValidationException">Thrown when the phone number is already taken.</exception>
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

    /// <summary>
    /// Maps a user entity to its corresponding DTO representation.
    /// </summary>
    private UserDto MapToDto(UserDetail user)
    {
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

    /// <summary>
    /// Updates the current user's own profile fields, ensuring the new email and phone number
    /// are not already used by a different account.
    /// </summary>
    /// <param name="userId">The identifier of the user updating their profile.</param>
    /// <param name="request">The updated profile details.</param>
    /// <returns>The updated user DTO, or null if no user exists with the given id.</returns>
    /// <exception cref="AppValidationException">Thrown when the email and/or phone is already taken by another account.</exception>
    public async Task<UserDto?> UpdateProfileAsync(string userId, UpdateProfileDto request)
    {
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

    /// <summary>
    /// Changes a user's password after verifying their current password.
    /// </summary>
    /// <param name="userId">The identifier of the user changing their password.</param>
    /// <param name="request">The current and new password values.</param>
    /// <returns>True if the password was changed; false if no user exists with the given id.</returns>
    /// <exception cref="AppValidationException">Thrown when the current password is incorrect.</exception>
    public async Task<bool> ChangePasswordAsync(string userId, ChangePasswordDto request)
    {
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
