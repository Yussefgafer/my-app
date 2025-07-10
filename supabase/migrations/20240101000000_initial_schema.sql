-- Enable Row Level Security on all tables
ALTER TABLE IF EXISTS public.user_settings ENABLE ROW LEVEL SECURITY;
ALTER TABLE IF EXISTS public.settings_change_logs ENABLE ROW LEVEL SECURITY;

-- Create ENUM types
CREATE TYPE performance_mode AS ENUM ('POWER_SAVING', 'BALANCED', 'MAX_PERFORMANCE');
CREATE TYPE sync_status AS ENUM ('SYNCED', 'PENDING', 'ERROR', 'CONFLICT');

-- User Settings Table
CREATE TABLE IF NOT EXISTS public.user_settings (
    id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    dark_mode_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    performance_mode performance_mode NOT NULL DEFAULT 'BALANCED',
    relay_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    last_synced_at TIMESTAMPTZ DEFAULT NOW(),
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- Settings Change Logs Table
CREATE TABLE IF NOT EXISTS public.settings_change_logs (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    setting_name TEXT NOT NULL,
    old_value TEXT,
    new_value TEXT,
    sync_status sync_status NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_settings_user_id ON public.user_settings(id);
CREATE INDEX IF NOT EXISTS idx_change_logs_user_id ON public.settings_change_logs(user_id);
CREATE INDEX IF NOT EXISTS idx_change_logs_sync_status ON public.settings_change_logs(sync_status);

-- RLS Policies for user_settings
CREATE POLICY "Users can view their own settings"
    ON public.user_settings
    FOR SELECT
    USING (auth.uid() = id);

CREATE POLICY "Users can update their own settings"
    ON public.user_settings
    FOR UPDATE
    USING (auth.uid() = id);

-- RLS Policies for settings_change_logs
CREATE POLICY "Users can view their own change logs"
    ON public.settings_change_logs
    FOR SELECT
    USING (auth.uid() = user_id);

CREATE POLICY "Users can insert their own change logs"
    ON public.settings_change_logs
    FOR INSERT
    WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Users can update their own change logs"
    ON public.settings_change_logs
    FOR UPDATE
    USING (auth.uid() = user_id);

-- Create a function to update the updated_at column
CREATE OR REPLACE FUNCTION update_modified_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Create triggers to update updated_at on row update
CREATE TRIGGER update_user_settings_modtime
    BEFORE UPDATE ON public.user_settings
    FOR EACH ROW
    EXECUTE FUNCTION update_modified_column();

CREATE TRIGGER update_change_logs_modtime
    BEFORE UPDATE ON public.settings_change_logs
    FOR EACH ROW
    EXECUTE FUNCTION update_modified_column();

-- Create a function to handle new user registration
CREATE OR REPLACE FUNCTION public.handle_new_user()
RETURNS TRIGGER AS $$
BEGIN
    -- Insert default settings for new user
    INSERT INTO public.user_settings (id)
    VALUES (NEW.id);
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Create trigger to handle new user registration
CREATE OR REPLACE TRIGGER on_auth_user_created
    AFTER INSERT ON auth.users
    FOR EACH ROW
    EXECUTE FUNCTION public.handle_new_user();

-- Grant necessary permissions
GRANT USAGE ON SCHEMA public TO anon, authenticated, service_role;
GRANT ALL ON ALL TABLES IN SCHEMA public TO authenticated, service_role;
GRANT ALL ON ALL SEQUENCES IN SCHEMA public TO authenticated, service_role;
GRANT ALL ON ALL FUNCTIONS IN SCHEMA public TO authenticated, service_role;
