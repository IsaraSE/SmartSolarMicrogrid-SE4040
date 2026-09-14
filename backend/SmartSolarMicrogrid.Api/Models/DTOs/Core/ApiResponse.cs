/*
 * File Name: ApiResponse.cs
 * Project: Smart Solar Microgrid Trading System
 * Module: SE4040 Enterprise Application Development
 * Author: Isara
 * Description: Common API response wrapper for consistent JSON formatting.
 * Date: 2026-09-14
 */

namespace SmartSolarMicrogrid.Api.Models.DTOs.Core;

/// <summary>
/// A standardized API response wrapper.
/// </summary>
public class ApiResponse<T>
{
    public bool Success { get; set; }
    public string Message { get; set; } = string.Empty;
    public T? Data { get; set; }

    /// <summary>
    /// Creates a success response.
    /// </summary>
    public static ApiResponse<T> SuccessResponse(string message, T? data = default)
    {
        return new ApiResponse<T>
        {
            Success = true,
            Message = message,
            Data = data
        };
    }

    /// <summary>
    /// Creates an error response.
    /// </summary>
    public static ApiResponse<T> ErrorResponse(string message)
    {
        return new ApiResponse<T>
        {
            Success = false,
            Message = message,
            Data = default
        };
    }
}
