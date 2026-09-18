import api from './api';

export const prosumerService = {
  // Get all prosumers
  getAllProsumers: async () => {
    const response = await api.get('/Prosumers');
    return response.data;
  },

  // Get pending prosumers
  getPendingProsumers: async () => {
    const response = await api.get('/Prosumers/pending');
    return response.data;
  },

  // Get deactivated prosumers
  getDeactivatedProsumers: async () => {
    const response = await api.get('/Prosumers/deactivated');
    return response.data;
  },

  // Get prosumer by NIC
  getProsumerByNic: async (nic) => {
    const response = await api.get(`/Prosumers/${nic}`);
    return response.data;
  },

  // Activate prosumer
  activateProsumer: async (nic) => {
    const response = await api.put(`/Prosumers/${nic}/activate`);
    return response.data;
  },

  // Reactivate prosumer
  reactivateProsumer: async (nic) => {
    const response = await api.put(`/Prosumers/${nic}/reactivate`);
    return response.data;
  },

  // Deactivate prosumer
  deactivateProsumer: async (nic) => {
    const response = await api.put(`/Prosumers/${nic}/deactivate`);
    return response.data;
  }
};
