using MongoDB.Bson;
using MongoDB.Bson.Serialization.Attributes;
using SmartSolarMicrogrid.Api.Models.Enums;

namespace SmartSolarMicrogrid.Api.Models.Entities.Stations;

[BsonIgnoreExtraElements]
public class SolarStationInfo
{
    [BsonId]
    [BsonRepresentation(BsonType.ObjectId)]
    public string? StationId { get; set; }
    public string StationName { get; set; } = null!;
    public string Address { get; set; } = null!;
    public double Latitude { get; set; }
    public double Longitude { get; set; }
    public double Capacity { get; set; }
    public int BatterySlotCount { get; set; }
    public string OperatingStartTime { get; set; } = null!;
    public string OperatingEndTime { get; set; } = null!;
    
    [BsonIgnoreIfNull]
    public string? Description { get; set; }
    
    [BsonRepresentation(BsonType.String)]
    public StationStatus Status { get; set; }
}
