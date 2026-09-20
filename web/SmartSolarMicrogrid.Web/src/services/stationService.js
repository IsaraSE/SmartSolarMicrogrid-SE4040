import api from './api';

export const stationService = {
  getAllStations: async () => {
    try {
      const response = await api.get('/Stations');
      return response.data;
    } catch (error) {
      console.error('Error fetching stations:', error);
      throw error;
    }
  },

  getStationById: async (id) => {
    try {
      const response = await api.get(`/Stations/${id}`);
      return response.data;
    } catch (error) {
      console.error('Error fetching station details:', error);
      throw error;
    }
  },

  createStation: async (stationData) => {
    try {
      const response = await api.post('/Stations', stationData);
      return response.data;
    } catch (error) {
      console.error('Error creating station:', error);
      throw error;
    }
  },

  updateStation: async (id, stationData) => {
    try {
      const response = await api.put(`/Stations/${id}`, stationData);
      return response.data;
    } catch (error) {
      console.error('Error updating station:', error);
      throw error;
    }
  },

  deactivateStation: async (id) => {
    try {
      const response = await api.put(`/Stations/${id}/deactivate`, {});
      return response.data;
    } catch (error) {
      console.error('Error deactivating station:', error);
      throw error;
    }
  }
};
