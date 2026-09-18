import { useState, useEffect } from 'react';
import { useAuth } from '../../context/AuthContext';
import dashboardService from '../../services/dashboardService';
import { 
  LuCalendar, 
  LuSun, 
  LuUsers, 
  LuClock, 
  LuHardHat, 
  LuBatteryCharging,
  LuChartBar,
  LuServer,
  LuArrowUp,
  LuArrowDown
} from 'react-icons/lu';
import { 
  BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer,
  PieChart, Pie, Cell
} from 'recharts';
import './Dashboard.css';

const COLORS = ['#148b61', '#93c5fd', '#e2e8f0'];

const Dashboard = () => {
  const { user } = useAuth();
  const userName = user?.fullName?.split(' ')[0] || 'Tharindu';

  const [loading, setLoading] = useState(true);
  const [currentDate, setCurrentDate] = useState('');
  
  // KPIs
  const [activeProsumers, setActiveProsumers] = useState(0);
  const [approvedFutureCount, setApprovedFutureCount] = useState(0);
  const [gridOperators, setGridOperators] = useState(0);
  const [activeStations, setActiveStations] = useState(0);
  
  // Charts & Tables Data
  const [reservationsData, setReservationsData] = useState([]);
  const [utilizationData, setUtilizationData] = useState([
    { name: 'In Use', value: 0 },
    { name: 'Available', value: 0 },
    { name: 'Maintenance', value: 0 },
  ]);
  const [pendingReservationsList, setPendingReservationsList] = useState([]);

  useEffect(() => {
    // Set dynamic date
    const dateOptions = { weekday: 'short', day: '2-digit', month: 'short', year: 'numeric' };
    setCurrentDate(new Date().toLocaleDateString('en-GB', dateOptions));

    const fetchDashboardData = async () => {
      try {
        setLoading(true);
        // Fetch all data in parallel
        const [usersRes, prosumersRes, pendingRes, stationsRes, reservationsRes] = await Promise.all([
          dashboardService.getUsers(),
          dashboardService.getProsumers(),
          dashboardService.getPendingActivations(),
          dashboardService.getStations(),
          dashboardService.getReservations()
        ]);

        // Process KPI Data
        const allUsers = usersRes.data || usersRes || [];
        const allProsumers = prosumersRes.data || prosumersRes || [];
        const allPending = pendingRes.data || pendingRes || [];
        const allStations = stationsRes.data || stationsRes || [];
        const allReservations = reservationsRes.data || reservationsRes || [];

        // 1. Active Prosumers
        setActiveProsumers(allProsumers.filter(p => p.accountStatus === 'ACTIVE').length);

        // 2. Approved Future Bookings Count
        const futureApproved = allReservations.filter(r => 
          (r.status === 1 || r.status === 'APPROVED') && 
          new Date(r.scheduledStartDateTime) > new Date()
        ).length;
        setApprovedFutureCount(futureApproved);

        // 3. Grid Operators
        setGridOperators(allUsers.filter(u => u.role === 'GRID_OPERATOR').length);

        // 4. Active Stations
        setActiveStations(allStations.filter(s => s.status === 'ACTIVE').length);

        // Process Station Utilization (Donut Chart)
        let inUse = 0;
        let available = 0;
        let maintenance = 0;

        allStations.forEach(station => {
          if (station.status === 'DEACTIVATED') {
            maintenance++;
          } else if (station.status === 'ACTIVE') {
            // Check if it's currently in use (has an active reservation right now)
            const now = new Date();
            const isActiveNow = allReservations.some(r => 
              r.stationId === station.stationId && 
              (r.status === 1 || r.status === 'APPROVED') &&
              new Date(r.scheduledStartDateTime) <= now &&
              new Date(r.scheduledEndDateTime) >= now
            );
            
            if (isActiveNow) inUse++;
            else available++;
          }
        });

        const totalStations = allStations.length || 1; // prevent divide by zero
        setUtilizationData([
          { name: 'In Use', value: Math.round((inUse / totalStations) * 100) },
          { name: 'Available', value: Math.round((available / totalStations) * 100) },
          { name: 'Maintenance', value: Math.round((maintenance / totalStations) * 100) },
        ]);

        // Process Reservations Overview (Bar Chart) - Last 7 Days
        const last7Days = [];
        for (let i = 6; i >= 0; i--) {
          const d = new Date();
          d.setDate(d.getDate() - i);
          last7Days.push({
            date: d,
            name: d.toLocaleDateString('en-US', { month: 'short', day: '2-digit' }),
            Confirmed: 0,
            Pending: 0,
            Cancelled: 0
          });
        }

        allReservations.forEach(r => {
          const rDate = new Date(r.createdAt);
          const dayMatch = last7Days.find(d => d.date.getDate() === rDate.getDate() && d.date.getMonth() === rDate.getMonth());
          if (dayMatch) {
            if (r.status === 1 || r.status === 'APPROVED') dayMatch.Confirmed++;
            else if (r.status === 0 || r.status === 'PENDING') dayMatch.Pending++;
            else if (r.status === 2 || r.status === 'CANCELLED') dayMatch.Cancelled++;
          }
        });
        setReservationsData(last7Days);

        // Process Pending Reservations (Table)
        const pendingList = allReservations
          .filter(r => r.status === 0 || r.status === 'PENDING')
          .sort((a, b) => new Date(a.scheduledStartDateTime) - new Date(b.scheduledStartDateTime))
          .slice(0, 5)
          .map(r => {
            // Map Station ID to Station Name
            const station = allStations.find(s => s.stationId === r.stationId);
            
            // Format Date & Time
            const sDate = new Date(r.scheduledStartDateTime);
            const dateStr = sDate.toLocaleDateString('en-US', { month: 'short', day: '2-digit', year: 'numeric' });
            const timeStr = sDate.toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit' });

            return {
              id: r.reservationId,
              dateStr,
              timeStr,
              stationName: station ? station.stationName : 'Unknown Station',
              prosumerNic: r.prosumerNic,
              status: r.status
            };
          });
          
        setPendingReservationsList(pendingList);

      } catch (error) {
        console.error("Failed to load dashboard data", error);
      } finally {
        setLoading(false);
      }
    };

    fetchDashboardData();
  }, []);

  const getStatusBadgeClass = (status) => {
    if (status === 1 || status === 'APPROVED') return 'badge confirmed';
    if (status === 0 || status === 'PENDING') return 'badge pending';
    if (status === 2 || status === 'CANCELLED') return 'badge cancelled'; 
    return 'badge scheduled';
  };

  const getStatusText = (status) => {
    if (status === 0) return 'Pending';
    if (status === 1) return 'Approved';
    if (status === 2) return 'Cancelled';
    if (status === 3) return 'Completed';
    return String(status).charAt(0).toUpperCase() + String(status).slice(1).toLowerCase();
  };

  return (
    <div className="dashboard-container">
      {/* Header */}
      <div className="dashboard-header-container">
        <div className="dashboard-welcome">
          <h1>Welcome back, {userName}!</h1>
          <p>Monitor and manage your solar microgrid network in real time.</p>
        </div>
        <div className="dashboard-date-weather">
          <div className="dashboard-date">
            <LuCalendar />
            <div className="date-text">
              <h4>{currentDate}</h4>
              <p>Good to see you today.</p>
            </div>
          </div>
          <div className="dashboard-weather">
            <LuSun />
            <p>A cleaner<br />tomorrow is possible.</p>
          </div>
        </div>
      </div>

      {/* Stat Cards */}
      <div className="stat-cards-grid">
        <div className="stat-card" style={{ background: '#f0fdf4', borderColor: '#dcfce7' }}>
          <div className="stat-card-header">
            <div className="stat-icon" style={{ background: '#dcfce7', color: '#148b61' }}><LuUsers /></div>
            <h3>Active Prosumers</h3>
          </div>
          <div className="stat-value-row">
            <span className="stat-value">{loading ? '...' : activeProsumers}</span>
            <div className="stat-trend">
              <span className="trend-val positive"><LuArrowUp /> Live</span>
              <span className="trend-desc">Real-time data</span>
            </div>
          </div>
        </div>

        <div className="stat-card" style={{ background: '#fefce8', borderColor: '#fef08a' }}>
          <div className="stat-card-header">
            <div className="stat-icon" style={{ background: '#fef08a', color: '#ca8a04' }}><LuClock /></div>
            <h3>Approved Future Bookings</h3>
          </div>
          <div className="stat-value-row">
            <span className="stat-value">{loading ? '...' : approvedFutureCount}</span>
            <div className="stat-trend">
              <span className="trend-val neutral"><LuArrowUp /> Live</span>
              <span className="trend-desc">Real-time data</span>
            </div>
          </div>
        </div>

        <div className="stat-card" style={{ background: '#eff6ff', borderColor: '#dbeafe' }}>
          <div className="stat-card-header">
            <div className="stat-icon" style={{ background: '#dbeafe', color: '#2563eb' }}><LuHardHat /></div>
            <h3>Grid Operators</h3>
          </div>
          <div className="stat-value-row">
            <span className="stat-value">{loading ? '...' : gridOperators}</span>
            <div className="stat-trend">
              <span className="trend-val neutral"><LuArrowUp /> Live</span>
              <span className="trend-desc">Real-time data</span>
            </div>
          </div>
        </div>

        <div className="stat-card" style={{ background: '#f8fafc', borderColor: '#e2e8f0' }}>
          <div className="stat-card-header">
            <div className="stat-icon" style={{ background: '#e2e8f0', color: '#10b981' }}><LuBatteryCharging /></div>
            <h3>Active Stations</h3>
          </div>
          <div className="stat-value-row">
            <span className="stat-value">{loading ? '...' : activeStations}</span>
            <div className="stat-trend">
              <span className="trend-val positive"><LuArrowUp /> Live</span>
              <span className="trend-desc">Real-time data</span>
            </div>
          </div>
        </div>
      </div>

      {/* Charts */}
      <div className="charts-grid">
        {/* Reservations Overview */}
        <div className="card">
          <div className="card-header">
            <div className="card-title">
              <LuChartBar />
              <h3>Reservations Overview</h3>
            </div>
            <select className="card-select">
              <option>Last 7 days</option>
            </select>
          </div>
          <div style={{ width: '100%', height: 250 }}>
            {loading ? (
              <div style={{height: '100%', display: 'flex', alignItems: 'center', justifyContent: 'center', color: '#94a3b8'}}>Loading chart data...</div>
            ) : (
              <ResponsiveContainer>
                <BarChart data={reservationsData} margin={{ top: 20, right: 0, left: -20, bottom: 0 }}>
                  <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#e2e8f0" />
                  <XAxis dataKey="name" axisLine={false} tickLine={false} tick={{ fontSize: 11, fill: '#64748b' }} dy={10} />
                  <YAxis axisLine={false} tickLine={false} tick={{ fontSize: 11, fill: '#64748b' }} />
                  <Tooltip cursor={{fill: '#f8fafc'}} />
                  <Bar dataKey="Confirmed" stackId="a" fill="#10b981" barSize={12} radius={[0, 0, 4, 4]} />
                  <Bar dataKey="Pending" stackId="a" fill="#facc15" />
                  <Bar dataKey="Cancelled" stackId="a" fill="#60a5fa" radius={[4, 4, 0, 0]} />
                </BarChart>
              </ResponsiveContainer>
            )}
          </div>
          <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '16px', marginTop: '10px', fontSize: '0.75rem', color: '#64748b', fontWeight: '500' }}>
            <span style={{ display: 'flex', alignItems: 'center', gap: '6px' }}><div style={{width: 8, height: 8, borderRadius: 2, background: '#10b981'}}></div> Confirmed</span>
            <span style={{ display: 'flex', alignItems: 'center', gap: '6px' }}><div style={{width: 8, height: 8, borderRadius: 2, background: '#facc15'}}></div> Pending</span>
            <span style={{ display: 'flex', alignItems: 'center', gap: '6px' }}><div style={{width: 8, height: 8, borderRadius: 2, background: '#60a5fa'}}></div> Cancelled</span>
          </div>
        </div>

        {/* Station Utilization */}
        <div className="card">
          <div className="card-header">
            <div className="card-title">
              <LuServer />
              <h3>Station Utilization</h3>
            </div>
          </div>
          <div style={{ display: 'flex', alignItems: 'center' }}>
            <div style={{ width: '150px', height: '150px', position: 'relative' }}>
              {!loading && (
                <ResponsiveContainer>
                  <PieChart>
                    <Pie
                      data={utilizationData}
                      innerRadius={55}
                      outerRadius={70}
                      paddingAngle={2}
                      dataKey="value"
                      stroke="none"
                    >
                      {utilizationData.map((entry, index) => (
                        <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                      ))}
                    </Pie>
                  </PieChart>
                </ResponsiveContainer>
              )}
              <div style={{ position: 'absolute', top: 0, left: 0, width: '100%', height: '100%', display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center' }}>
                <span style={{ fontSize: '1.4rem', fontWeight: '700', color: '#1a233a' }}>{loading ? '-' : utilizationData[0].value}%</span>
                <span style={{ fontSize: '0.6rem', color: '#64748b' }}>In Use</span>
              </div>
            </div>
            <div style={{ flex: 1, paddingLeft: '20px', display: 'flex', flexDirection: 'column', gap: '12px' }}>
              {utilizationData.map((item, index) => (
                <div key={item.name} style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', fontSize: '0.8rem' }}>
                  <span style={{ display: 'flex', alignItems: 'center', gap: '8px', color: '#64748b', fontWeight: '500' }}>
                    <div style={{ width: 8, height: 8, borderRadius: 2, background: COLORS[index] }}></div>
                    {item.name}
                  </span>
                  <span style={{ fontWeight: '700', color: '#1a233a' }}>{loading ? '-' : item.value}%</span>
                </div>
              ))}
            </div>
          </div>
          <div style={{ marginTop: '20px', background: '#f0fdf4', borderRadius: '8px', padding: '12px 16px', display: 'flex', alignItems: 'center', gap: '12px' }}>
            <div style={{ width: 32, height: 32, background: '#dcfce7', borderRadius: '50%', display: 'flex', alignItems: 'center', justifyContent: 'center', color: '#148b61' }}>
              <LuBatteryCharging />
            </div>
            <div style={{ flex: 1 }}>
              <span style={{ fontSize: '0.85rem', fontWeight: '700', color: '#1a233a', display: 'flex', alignItems: 'center', gap: '8px' }}>
                Real-Time <LuArrowUp style={{ color: '#148b61' }} />
              </span>
              <span style={{ fontSize: '0.7rem', color: '#64748b' }}>Live data from network</span>
            </div>
          </div>
        </div>
      </div>

      {/* Tables - Single Table Now */}
      <div className="tables-grid">
        {/* Pending Reservations Table */}
        <div className="card full-width">
          <div className="card-header">
            <div className="card-title">
              <LuCalendar />
              <h3>Pending Reservations</h3>
            </div>
            <a href="/reservations" className="view-all">View All</a>
          </div>
          <table className="dashboard-table">
            <thead>
              <tr>
                <th>Date & Time</th>
                <th>Station</th>
                <th>Prosumer NIC</th>
                <th>Status</th>
              </tr>
            </thead>
            <tbody>
              {loading ? (
                <tr>
                  <td colSpan="4" style={{ textAlign: 'center', color: '#64748b', padding: '20px' }}>Loading activities...</td>
                </tr>
              ) : pendingReservationsList.length === 0 ? (
                <tr>
                  <td colSpan="4" style={{ textAlign: 'center', color: '#64748b', padding: '20px' }}>No pending reservations.</td>
                </tr>
              ) : pendingReservationsList.map((res, index) => (
                <tr key={index}>
                  <td>{res.dateStr}<span className="text-sub">{res.timeStr}</span></td>
                  <td>{res.stationName}</td>
                  <td>{res.prosumerNic}</td>
                  <td><span className={getStatusBadgeClass(res.status)}>{getStatusText(res.status)}</span></td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
      
      {/* Tiny footer disclaimer */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginTop: '20px', paddingTop: '20px', borderTop: '1px solid #e2e8f0' }}>
        <div>
          <p style={{ margin: 0, fontFamily: 'Inter', fontSize: '0.75rem', fontWeight: '600', color: '#1a233a' }}>Smart Solar Microgrid Trading System</p>
          <p style={{ margin: 0, fontFamily: 'Inter', fontSize: '0.7rem', color: '#64748b' }}>Building a smarter, cleaner, and more resilient energy future.</p>
        </div>
        <div style={{ display: 'flex', alignItems: 'center', gap: '8px', fontFamily: 'Inter', fontSize: '0.75rem', color: '#64748b', fontWeight: '500' }}>
          <LuSun style={{ color: '#10b981' }} /> Renewable People. Real Progress.
        </div>
      </div>
    </div>
  );
};

export default Dashboard;
