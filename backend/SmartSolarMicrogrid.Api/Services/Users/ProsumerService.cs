/*
 * File Name: ProsumerService.cs
 * Project: Smart Solar Microgrid Trading System
 * Module: SE4040 Enterprise Application Development
 * Author: IT22194862
 * Description: Implementation of ProsumerService.cs
 * Date: 2026-09-15
 */

using SmartSolarMicrogrid.Api.Models.DTOs;
using SmartSolarMicrogrid.Api.Models.Entities;
using SmartSolarMicrogrid.Api.Models.Enums;
using SmartSolarMicrogrid.Api.Repositories;
using SmartSolarMicrogrid.Api.Repositories.Users;

namespace SmartSolarMicrogrid.Api.Services.Users;

public class ProsumerService : IProsumerService
{
    private readonly IUserDetailsRepository _userRepository;
    private readonly IEnergyReservationRepository _reservationRepository;

    public ProsumerService(IUserDetailsRepository userRepository, IEnergyReservationRepository reservationRepository)
    {
        _userRepository = userRepository;
        _reservationRepository = reservationRepository;
    }

    /// <summary>
    /// Registers a new prosumer using NIC as the unique primary identifier.
    /// New accounts start in PENDING status until a Backoffice officer activates them.
    /// </summary>
    public async Task<(bool Success, string Message, UserDto? Prosumer)> RegisterProsumerAsync(RegisterProsumerDto request)
    {
        var existingByNic = await _userRepository.GetByNicAsync(request.Nic);
        if (existingByNic != null)
        {
            return (false, "A prosumer with this NIC already exists.", null);
        }

        var existingByEmail = await _userRepository.GetByEmailAsync(request.Email);
        if (existingByEmail != null)
        {
            return (false, "A user with this email already exists.", null);
        }

        // Phone is also unique in the database, so check it before inserting.
        var existingByPhone = await _userRepository.FindAsync(u => u.Phone == request.Phone);
        if (existingByPhone.Any())
        {
            return (false, "A user with this phone number already exists.", null);
        }

        var user = new UserDetail
        {
            UserId = MongoDB.Bson.ObjectId.GenerateNewId().ToString(),
            Nic = request.Nic,
            FullName = request.FullName,
            Email = request.Email,
            Phone = request.Phone,
            PasswordHash = BCrypt.Net.BCrypt.HashPassword(request.Password),
            Role = UserRole.PROSUMER,
            AccountStatus = AccountStatus.PENDING,
            Address = request.Address,
            CreatedAt = DateTime.UtcNow
        };

        try
        {
            await _userRepository.CreateAsync(user);
        }
        catch (MongoDB.Driver.MongoWriteException ex)
            when (ex.WriteError?.Category == MongoDB.Driver.ServerErrorCategory.DuplicateKey)
        {
            // Safety net for any unique-index conflict (email, NIC, phone) not caught above.
            return (false, "A user with the same email, NIC, or phone number already exists.", null);
        }

        return (true, "Prosumer registered successfully.", MapToDto(user));
    }

    /// <summary>
    /// Updates a prosumer's own editable profile fields (name, email, phone, address).
    /// </summary>
    public async Task<(bool Success, string Message, UserDto? Prosumer)> UpdateProsumerProfileAsync(string nic, UpdateProsumerDto request)
    {
        var user = await _userRepository.GetByNicAsync(nic);
        if (user == null || user.Role != UserRole.PROSUMER)
        {
            return (false, "Prosumer not found.", null);
        }

        // Prevent claiming an email already used by a different account.
        var existingByEmail = await _userRepository.GetByEmailAsync(request.Email);
        if (existingByEmail != null && existingByEmail.UserId != user.UserId)
        {
            return (false, "A user with this email already exists.", null);
        }

        user.FullName = request.FullName;
        user.Email = request.Email;
        user.Phone = request.Phone;
        user.Address = request.Address;

        await _userRepository.UpdateAsync(user.UserId!, user);
        return (true, "Profile updated successfully.", MapToDto(user));
    }

    public async Task<IEnumerable<UserDto>> GetAllProsumersAsync()
    {
        // Retrieves all prosumers data from the system.
        var users = await _userRepository.FindAsync(u => u.Role == UserRole.PROSUMER);
        return users.Select(MapToDto);
    }

    public async Task<UserDto?> GetProsumerByNicAsync(string nic)
    {
        // Retrieves prosumer by nic data from the system.
        var user = await _userRepository.GetByNicAsync(nic);
        if (user == null || user.Role != UserRole.PROSUMER) return null;
        return MapToDto(user);
    }

    public async Task<IEnumerable<UserDto>> GetPendingProsumersAsync()
    {
        // Retrieves pending prosumers data from the system.
        var users = await _userRepository.FindAsync(u => u.Role == UserRole.PROSUMER && u.AccountStatus == AccountStatus.PENDING);
        return users.Select(MapToDto);
    }

    public async Task<UserDto?> ActivateProsumerAsync(string nic)
    {
        // Activates the specified prosumer.
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
        // Retrieves deactivated prosumers data from the system.
        var users = await _userRepository.FindAsync(u => u.Role == UserRole.PROSUMER && u.AccountStatus == AccountStatus.DEACTIVATED);
        return users.Select(MapToDto);
    }

    public async Task<UserDto?> ReactivateProsumerAsync(string nic)
    {
        // Executes logic to reactivate prosumer.
        var user = await _userRepository.GetByNicAsync(nic);
        if (user == null || user.Role != UserRole.PROSUMER || user.AccountStatus != AccountStatus.DEACTIVATED)
        {
            return null;
        }

        user.AccountStatus = AccountStatus.ACTIVE;
        await _userRepository.UpdateAsync(user.UserId!, user);
        return MapToDto(user);
    }

    public async Task<(bool Success, string Message, UserDto? Prosumer)> DeactivateProsumerAsync(string nic)
    {
        var user = await _userRepository.GetByNicAsync(nic);
        if (user == null || user.Role != UserRole.PROSUMER || user.AccountStatus != AccountStatus.ACTIVE)
        {
            return (false, "Prosumer not found or not in active status.", null);
        }

        var allReservations = await _reservationRepository.GetAllAsync();
        var ongoingReservations = allReservations.Where(r => r.ProsumerNic == nic && (r.Status == ReservationStatus.PENDING || r.Status == ReservationStatus.APPROVED)).ToList();
        
        if (ongoingReservations.Any())
        {
            return (false, "You cannot deactivate your account because you have ongoing bookings. Please complete or cancel them first.", null);
        }

        user.AccountStatus = AccountStatus.DEACTIVATED;
        await _userRepository.UpdateAsync(user.UserId!, user);
        return (true, "Prosumer deactivated successfully.", MapToDto(user));
    }

    private static UserDto MapToDto(UserDetail user)
    {
        // Maps to dto to the corresponding DTO.
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
