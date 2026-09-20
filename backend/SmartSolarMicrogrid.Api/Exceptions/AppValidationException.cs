/*
 * File Name: AppValidationException.cs
 * Project: Smart Solar Microgrid Trading System
 * Module: SE4040 Enterprise Application Development
 * Author: Isara
 * Description: Custom exception for throwing field-specific validation errors.
 * Date: 2026-09-18
 */

namespace SmartSolarMicrogrid.Api.Exceptions;

public class AppValidationException : Exception
{
    public Dictionary<string, string[]> Errors { get; }

    public AppValidationException(Dictionary<string, string[]> errors) 
        : base("One or more validation errors occurred.")
    {
        Errors = errors;
    }
}
