/*
 * File Name: IAuthService.cs
 * Project: Smart Solar Microgrid Trading System
 * Module: SE4040 Enterprise Application Development
 * Author: Isara
 * Description: Service interface for authentication logic.
 * Date: 2026-09-14
 */

using SmartSolarMicrogrid.Api.Models.DTOs;

namespace SmartSolarMicrogrid.Api.Services.Users;

public interface IAuthService
{
    Task<LoginResponseDto?> LoginAsync(LoginRequestDto request);
}
