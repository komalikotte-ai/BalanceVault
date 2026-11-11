import readline from 'readline';
import { bootstrapService } from '../services/bootstrap.service.js';
import { bootstrapAdminSchema } from '../schemas/bootstrap.schema.js';
import { prisma } from '../config/prisma.js';

function askQuestion(query: string): Promise<string> {
  const rl = readline.createInterface({
    input: process.stdin,
    output: process.stdout,
  });

  return new Promise((resolve) => {
    rl.question(query, (answer) => {
      rl.close();
      resolve(answer.trim());
    });
  });
}

async function runCliBootstrap(): Promise<void> {
  console.log('============================================================');
  console.log('BalanceVault - First Administrator Bootstrap CLI');
  console.log('============================================================\n');

  try {
    const status = await bootstrapService.canBootstrap();

    if (!status.canBootstrap) {
      console.error('Bootstrap is not permitted.');
      console.error(status.reason ?? 'Unknown reason.');
      return;
    }

    console.log('No administrator account exists. Bootstrap is available.\n');
    console.log('Note: Terminal input will be visible while entering credentials.\n');

    const bootstrapSecret = await askQuestion('Bootstrap secret: ');
    const email = await askQuestion('Administrator email: ');
    const fullName = await askQuestion('Administrator full name: ');
    const password = await askQuestion('Administrator password: ');

    const input = bootstrapAdminSchema.parse({
      bootstrapSecret,
      email,
      fullName,
      password,
    });

    console.log('\nInitializing root administrator...');

    const admin = await bootstrapService.bootstrapAdmin(
      input,
      {
        ipAddress: '127.0.0.1 (CLI)',
        userAgent: 'BalanceVault-Admin-CLI/1.0',
      }
    );

    console.log('\nRoot Administrator Initialized Successfully!');
    console.log(`ID:        ${admin.id}`);
    console.log(`Email:     ${admin.email}`);
    console.log(`Full Name: ${admin.fullName}`);
    console.log(`Role:      ${admin.role}`);
    console.log(`Status:    ${admin.status}`);
    console.log('\nBootstrap is now locked.');
  } catch (error: unknown) {
    if (error instanceof Error) {
      console.error('\nBootstrap failed:', error.message);
    } else {
      console.error('\nBootstrap failed due to an unknown error.');
    }
    process.exitCode = 1;
  } finally {
    await prisma.$disconnect();
  }
}

void runCliBootstrap();
