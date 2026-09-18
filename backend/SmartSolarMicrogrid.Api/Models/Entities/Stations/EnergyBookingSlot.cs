using MongoDB.Bson;
using MongoDB.Bson.Serialization.Attributes;
using SmartSolarMicrogrid.Api.Models.Enums;
using System;

namespace SmartSolarMicrogrid.Api.Models.Entities.Stations;

[BsonIgnoreExtraElements]
public class EnergyBookingSlot
{
    [BsonId]
    [BsonRepresentation(BsonType.ObjectId)]
    public string? SlotId { get; set; }

    public string SlotName { get; set; } = null!;
    
    [BsonRepresentation(BsonType.ObjectId)]
    public string StationId { get; set; } = null!;
    
    public DateTime StartDateTime { get; set; }
    public DateTime EndDateTime { get; set; }
    
    [BsonRepresentation(BsonType.String)]
    public SlotStatus Status { get; set; }
    
    [BsonIgnoreIfNull]
    public string? ReservedBy { get; set; }
    
    [BsonIgnoreIfNull]
    public string? Notes { get; set; }
}
