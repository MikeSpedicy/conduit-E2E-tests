import { Injectable } from '@nestjs/common';
import { Client, mapping, auth } from 'cassandra-driver';

@Injectable()
export class CassandraService {
  client: Client;
  mapper: mapping.Mapper;

  private createClient() {
    // Local Connection
    // this.client = new Client({
    //   contactPoints: ['localhost'],
    //   keyspace: 'conduit',
    //   localDataCenter: 'datacenter1',
    //   authProvider: new auth.PlainTextAuthProvider('cassandra', 'cassandra'),
    // });

    // Astra connection
    this.client = new Client({
      // for example of the cassandra database see "dataset" folder in root of the repo.
      cloud: {
        secureConnectBundle:
          // Astra's DB secure-connect data
          './libs/cassandra-service/src/lib/secure-connect-conduit-test-db.zip',
      },
      credentials: {
        // Astra's clientId value
        username: process.env.astraUsername || 'your value',
        // Astra's secret value
        password: process.env.astraPassword || 'your value',
      },
      // keyspace of your DB
      keyspace: 'conduit_test_db',
    });
  }

  createMapper(mappingOptions: mapping.MappingOptions) {
    if (this.client == undefined) {
      this.createClient();
      this.client.connect();
    }
    return new mapping.Mapper(this.client, mappingOptions);
  }
}
