/*
 * File Name: SmartSolarMicrogridDatabaseSettings.cs
 * Project: Smart Solar Microgrid Trading System
 * Module: SE4040 Enterprise Application Development
 * Author: IT22154880
 * Description: Implementation of SmartSolarMicrogridDatabaseSettings.cs
 * Date: 2026-09-24
 */

namespace SmartSolarMicrogrid.Api.Models;

public class SmartSolarMicrogridDatabaseSettings
{
    public string ConnectionString { get; set; } = null!;
    public string DatabaseName { get; set; } = null!;
}
