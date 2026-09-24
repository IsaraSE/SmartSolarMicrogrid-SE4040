/*
 * File Name: IAuthService.cs
 * Project: Smart Solar Microgrid Trading System
 * Module: SE4040 Enterprise Application Development
 * Author: IT22194862
 * Description: Implementation of IAuthService.cs
 * Date: 2026-09-23
 */

using SmartSolarMicrogrid.Api.Models.DTOs;
using System.Threading.Tasks;

namespace SmartSolarMicrogrid.Api.Services.Users;

public interface IAuthService
{
    Task<(bool Success, string Message, LoginResponseDto? Data)> LoginAsync(LoginRequestDto request);
}
