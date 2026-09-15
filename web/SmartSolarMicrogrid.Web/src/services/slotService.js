import api from './api';

export const slotService = {
  // Get all slots for a station
  getSlotsByStationId: async (stationId) => {
    try {
      const response = await api.get(`/Slots/station/${stationId}`);
      return response.data;
    } catch (error) {
      console.error('Error fetching slots:', error);
      throw error;
    }
  },

  // Get available slots for a station
  getAvailableSlotsByStationId: async (stationId) => {
    try {
      const response = await api.get(`/Slots/station/${stationId}/available`);
      return response.data;
    } catch (error) {
      console.error('Error fetching available slots:', error);
      throw error;
    }
  },

  // Create a new slot
  createSlot: async (slotData) => {
    try {
      const response = await api.post('/Slots', slotData);
      return response.data;
    } catch (error) {
      console.error('Error creating slot:', error);
      throw error;
    }
  },

  // Update a slot
  updateSlot: async (id, slotData) => {
    try {
      const response = await api.put(`/Slots/${id}`, slotData);
      return response.data;
    } catch (error) {
      console.error('Error updating slot:', error);
      throw error;
    }
  },

  // Delete a slot
  deleteSlot: async (id) => {
    try {
      const response = await api.delete(`/Slots/${id}`);
      return response.data;
    } catch (error) {
      console.error('Error deleting slot:', error);
      throw error;
    }
  }
};
