import { useState } from 'react';
import { Navbar } from './components/Navbar';
import { RepositoryDashboard } from './features/repositories/RepositoryDashboard';
import { ChatView } from './features/chat/ChatView';
import { useGetRepositoryByIdQuery } from './features/api/apiSlice';

export function App() {
  const [currentView, setCurrentView] = useState<'dashboard' | 'chat'>('dashboard');
  const [selectedRepoId, setSelectedRepoId] = useState<string | null>(null);

  const { data: activeRepo } = useGetRepositoryByIdQuery(selectedRepoId!, {
    skip: !selectedRepoId,
  });

  const handleOpenAssistant = (repoId: string) => {
    setSelectedRepoId(repoId);
    setCurrentView('chat');
  };

  const handleNavigateHome = () => {
    setCurrentView('dashboard');
  };

  return (
    <div className="min-h-screen bg-[#070a12] text-slate-100 flex flex-col font-sans selection:bg-blue-600/30 selection:text-blue-200">
      <Navbar
        currentView={currentView}
        onNavigateHome={handleNavigateHome}
        activeRepoName={activeRepo?.name}
      />

      <div className="flex-1">
        {currentView === 'dashboard' ? (
          <RepositoryDashboard onOpenAssistant={handleOpenAssistant} />
        ) : (
          selectedRepoId && (
            <ChatView
              repoId={selectedRepoId}
              onBack={handleNavigateHome}
            />
          )
        )}
      </div>
    </div>
  );
}

export default App;
