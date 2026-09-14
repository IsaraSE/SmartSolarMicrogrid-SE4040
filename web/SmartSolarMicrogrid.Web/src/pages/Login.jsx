import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import {
  FiUser,
  FiLock,
  FiEye,
  FiEyeOff,
  FiArrowRight,
  FiCheck,
  FiAlertCircle,
  FiShield,
} from 'react-icons/fi';
import { PiLeafLight, PiUsersLight, PiChartBarLight } from 'react-icons/pi';
import solarHeroBg from '../assets/images/solar-hero-bg.jpg';
import './Login.css';

import leafLogo from '../assets/images/leaf-logo.png';

const Login = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [rememberMe, setRememberMe] = useState(true);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const { login } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      const response = await login(email, password);
      if (response.success) {
        navigate('/dashboard');
      } else {
        setError(response.message || 'Login failed. Please try again.');
      }
    } catch (err) {
      const message =
        err.response?.data?.message ||
        'Unable to connect to server. Please try again.';
      setError(message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-page">
      {/* ========== LEFT HERO PANEL ========== */}
      <div className="login-hero">
        <img
          src={solarHeroBg}
          alt="Solar panels at sunset"
          className="login-hero-bg"
        />

        {/* Hero Header */}
        <div className="hero-header">
          <div className="hero-brand">
            <img src={leafLogo} alt="Smart Solar Logo" className="hero-brand-icon" />
            <div className="hero-brand-text">
              <h2>Smart Solar</h2>
              <p>Microgrid Trading System</p>
            </div>
          </div>
          <div className="hero-tagline">
            CLEAN ENERGY
            <br />
            STRONGER
            <br />
            COMMUNITIES
            <div className="hero-tagline-line"></div>
          </div>
        </div>

        {/* Hero Content */}
        <div className="hero-content">
          <h1>
            Trade Smarter.
            <br />
            Power a Brighter
            <br />
            <span className="highlight">Tomorrow.</span>
          </h1>
          <p className="hero-subtitle">
            A smarter, cleaner, and more resilient
            <br />
            energy future through trusted
            <br />
            microgrid trading.
          </p>

          <div className="hero-features">
            <div className="hero-feature-item">
              <div className="hero-feature-icon">
                <PiLeafLight />
              </div>
              <span>
                Renewable
                <br />
                Energy Trading
              </span>
            </div>
            <div className="hero-feature-item">
              <div className="hero-feature-icon">
                <PiUsersLight />
              </div>
              <span>
                Connected
                <br />
                Communities
              </span>
            </div>
            <div className="hero-feature-item">
              <div className="hero-feature-icon">
                <PiChartBarLight />
              </div>
              <span>
                A More Resilient
                <br />
                Tomorrow
              </span>
            </div>
          </div>
        </div>

        {/* Hero Footer */}
        <div className="hero-footer">
          <p>
            SUSTAINABLE ENERGY
            <br />
            BRIGHTER LIVES
            <div className="hero-footer-line"></div>
          </p>
        </div>
      </div>

      {/* ========== RIGHT LOGIN PANEL ========== */}
      <div className="login-panel">
        <div className="watermark-container">
          <img src={leafLogo} alt="Watermark" className="watermark-logo" />
        </div>
        <div className="login-card-container">
          <div className="login-card">
            {/* Login Brand */}
            <div className="login-brand login-brand-center">
              <img src={leafLogo} alt="Smart Solar Logo" className="login-brand-icon" />
              <div className="login-brand-text login-brand-text-dark">
                <h2>Smart Solar</h2>
                <p>Microgrid Trading System</p>
              </div>
            </div>

            {/* Login Heading */}
            <div className="login-heading">
              <h1>Welcome Back</h1>
              <p>Sign in to access the Smart Solar Microgrid Trading System.</p>
            </div>

            {/* Login Form */}
            <form className="login-form" onSubmit={handleSubmit}>
              {/* Error Message */}
              {error && (
                <div className="error-message">
                  <FiAlertCircle />
                  <span>{error}</span>
                </div>
              )}

              {/* Email Field */}
              <div className="form-group">
                <label htmlFor="email">Email or Username</label>
                <div className="input-wrapper">
                  <span className="input-icon">
                    <FiUser />
                  </span>
                  <input
                    id="email"
                    type="text"
                    placeholder="Enter your email or username"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    required
                    autoComplete="email"
                  />
                </div>
              </div>

              {/* Password Field */}
              <div className="form-group">
                <label htmlFor="password">Password</label>
                <div className="input-wrapper">
                  <span className="input-icon">
                    <FiLock />
                  </span>
                  <input
                    id="password"
                    type={showPassword ? 'text' : 'password'}
                    placeholder="Enter your password"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    required
                    autoComplete="current-password"
                  />
                  <button
                    type="button"
                    className="toggle-password"
                    onClick={() => setShowPassword(!showPassword)}
                    aria-label={showPassword ? 'Hide password' : 'Show password'}
                  >
                    {showPassword ? <FiEyeOff /> : <FiEye />}
                  </button>
                </div>
              </div>

              {/* Form Options */}
              <div className="form-options">
                <label className="remember-me">
                  <input
                    type="checkbox"
                    checked={rememberMe}
                    onChange={(e) => setRememberMe(e.target.checked)}
                  />
                  <span className="checkbox-custom">
                    <FiCheck />
                  </span>
                  <span>Remember me</span>
                </label>
                <a href="#" className="forgot-password">
                  Forgot password?
                </a>
              </div>

              {/* Sign In Button */}
              <button
                type="submit"
                className="login-btn"
                disabled={loading || !email || !password}
              >
                {loading ? (
                  <div className="spinner" />
                ) : (
                  <>
                    Sign In <FiArrowRight />
                  </>
                )}
              </button>

              {/* Access Notice */}
              <div className="access-notice">
                <div className="access-notice-icon">
                  <FiShield />
                </div>
                <p>Authorized access for Backoffice and Grid Operators only.</p>
              </div>
            </form>
          </div>
        </div>

        {/* Login Footer */}
        <div className="login-footer">
          <div className="login-footer-divider">
            <span>Renewable People. Real Progress.</span>
          </div>
          <p>
            Smart Solar Microgrid Trading System
            <br />© 2025. All rights reserved.
          </p>
        </div>
      </div>
    </div>
  );
};

export default Login;
