/*
 * File Name: AuthService.cs
 * Project: Smart Solar Microgrid Trading System
 * Module: SE4040 Enterprise Application Development
 * Author: Isara
 * Description: Service implementation for authentication logic including JWT generation.
 * Date: 2026-09-14
 */

using System;
using System.Collections.Generic;
using System.IdentityModel.Tokens.Jwt;
using System.Security.Claims;
using System.Text;
using System.Threading.Tasks;
using Microsoft.Extensions.Options;
using Microsoft.IdentityModel.Tokens;
using SmartSolarMicrogrid.Api.Models;
using SmartSolarMicrogrid.Api.Models.DTOs;
using SmartSolarMicrogrid.Api.Models.Enums;
using SmartSolarMicrogrid.Api.Repositories.Users;

namespace SmartSolarMicrogrid.Api.Services.Users;

public class AuthService : IAuthService
{
    private readonly IUserDetailsRepository _userRepository;
    private readonly JwtSettings _jwtSettings;

    public AuthService(IUserDetailsRepository userRepository, IOptions<JwtSettings> jwtSettings)
    {
        _userRepository = userRepository;
        _jwtSettings = jwtSettings.Value;
    }

    public async Task<(bool Success, string Message, LoginResponseDto? Data)> LoginAsync(LoginRequestDto request)
    {
        var user = await _userRepository.GetByEmailAsync(request.Email);
        
        // Block Prosumers from logging in with an email
        if (user != null && user.Role == UserRole.PROSUMER)
        {
            return (false, "Prosumers must log in using their NIC.", null);
        }

        // Check by NIC if email not found
        if (user == null)
        {
            user = await _userRepository.GetByNicAsync(request.Email);
        }

        if (user == null)
        {
            return (false, "Invalid email/NIC or password.", null);
        }

        if (user.AccountStatus == AccountStatus.DEACTIVATED)
        {
            return (false, "Your account is currently deactivated. Please contact Backoffice for reactivation.", null);
        }

        // Verify password hash
        if (!BCrypt.Net.BCrypt.Verify(request.Password, user.PasswordHash))
        {
            return (false, "Invalid email/NIC or password.", null);
        }

        var tokenHandler = new JwtSecurityTokenHandler();
        var key = Encoding.ASCII.GetBytes(_jwtSettings.SecretKey);
        
        var claims = new List<Claim>
        {
            new Claim(ClaimTypes.NameIdentifier, user.UserId!),
            new Claim(ClaimTypes.Email, user.Email),
            new Claim(ClaimTypes.Role, user.Role.ToString())
        };

        if (!string.IsNullOrEmpty(user.Nic))
        {
            claims.Add(new Claim("nic", user.Nic));
        }

        var tokenDescriptor = new SecurityTokenDescriptor
        {
            Subject = new ClaimsIdentity(claims),
            Expires = DateTime.UtcNow.AddMinutes(_jwtSettings.ExpiryMinutes),
            Issuer = _jwtSettings.Issuer,
            Audience = _jwtSettings.Audience,
            SigningCredentials = new SigningCredentials(new SymmetricSecurityKey(key), SecurityAlgorithms.HmacSha256Signature)
        };

        var token = tokenHandler.CreateToken(tokenDescriptor);

        var response = new LoginResponseDto
        {
            UserId = user.UserId!,
            Nic = user.Nic ?? "",
            FullName = user.FullName,
            Role = user.Role,
            AccountStatus = user.AccountStatus,
            Token = tokenHandler.WriteToken(token)
        };

        return (true, "Login successful", response);
    }
}
